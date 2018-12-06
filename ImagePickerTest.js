import React from 'react';
import {
    View,
    Text,
    Button,
    Platform,
    Image,
} from 'react-native';

import Toast, { DURATION } from 'react-native-easy-toast';
import OpenCV from './OpenCV';
import ImagePicker from 'react-native-image-picker';

// More info on all the options is below in the API Reference... just some common use cases shown here
const options = {
    title: 'Select Image of Card',
    customButtons: [{ name: 'fb', title: 'Choose Photo from Facebook' }],
    storageOptions: {
        skipBackup: true,
        path: 'images',
    },
};


class ImagePickerTest extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            cardImageSource: '',
            photoAsBase64: {}
        }
    }

    onButtonPress = () => {
        console.log('Button was pressed');
        ImagePicker.showImagePicker(options, (response) => {
            console.log('Response = ', response);
            if (response.didCancel) {
                console.log('User cancelled image picker');
            } else if (response.error) {
                console.log('ImagePicker Error: ', response.error);
            } else if (response.customButton) {
                console.log('User tapped custom button: ', response.customButton);
            } else {
                const source = { uri: response.uri };
                // You can also display the image using data:
                // const source = { uri: 'data:image/jpeg;base64,' + response.data };
                this.setState({
                    ...this.state,
                    cardImageSource: source,
                    photoAsBase64: { content: response.data, photoPath: response.uri },
                });
                this.proceedWithFindingBlobs();
            }
        });
    }


    proceedWithFindingBlobs() {
        const { content, photoPath } = this.state.photoAsBase64;
        this.findBlobs(content).then(blobsFound => {
            console.log('blobsFound', blobsFound);
            if (blobsFound) {
                this.refs.toast.show('blobs found', DURATION.FOREVER);
                // return this.repeatPhoto();
            } else {
                this.refs.toast.show('blobs not found', DURATION.FOREVER);
            }
            // this.setState({ photoAsBase64: { ...this.state.photoAsBase64, isPhotoPreview: true, photoPath } });
        }).catch(err => {
            console.log('err', err)
        });
    }

    findBlobs(imageAsBase64) {
        console.log('Finding blobs in the image');
        return new Promise((resolve, reject) => {
            if (Platform.OS === 'android') {
                OpenCV.findBlobs(imageAsBase64, error => {
                    // error handling
                    console.log('error', error);
                }, msg => {
                    resolve(msg);
                });
            } else if (Platform.OS === 'ios') {
                OpenCV.findBlobs(imageAsBase64, (error, dataArray) => {
                    resolve(dataArray[0]);
                });
            }
        });
    }
    
    
    render() {
        return (
            <View>
                <Text>Please choose the image of card to analyze:</Text>
                <Button title="Choose Image" onPress={this.onButtonPress} />
                <Toast ref="toast" position="center" />
            </View>
        );
    }
}

export default ImagePickerTest;
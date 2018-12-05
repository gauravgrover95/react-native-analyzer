import React from 'react';
import {
    View,
    Text,
    Platform,
    Image,
    TouchableOpacity,
    StyleSheet,
} from 'react-native';

import {
    Svg,
    Circle,
} from 'react-native-svg';

import { RNCamera as Camera } from 'react-native-camera';
import Toast, { DURATION } from 'react-native-easy-toast';
import OpenCV from './OpenCV';
 
const CircleWithinCircle = () => (
    <Svg height="68" width="68">
        <Circle cx="34" cy="34" fill="#FFF" r="28" />
        <Circle cx="34" cy="34" fill="transparent" r="32" stroke="#fff" strokeWidth="2" />
    </Svg>
);

// function call goes like this:
// takePicture -> proceedWithCheckingBlurryImage -> checkForBlurryImage

class CameraTest extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            cameraPermission: false,
            photoAsBase64: {
                content: '',
                isPhotoPreview: false,
                photoPath: '',
            },
        };    

        console.log("Hello from CameraTest");
        console.log('OpenCV', OpenCV);
        console.log('OpenCV.dummy', OpenCV.dummy);
        // this.dummy = this.dummy.bind(this);
        
        this.takePicture = this.takePicture.bind(this);
        
        this.checkForBlurryImage = this.checkForBlurryImage.bind(this);
        this.proceedWithCheckingBlurryImage = this.proceedWithCheckingBlurryImage.bind(this);
        
        this.findBlobs = this.findBlobs.bind(this);
        this.proceedWithFindingBlobs = this.proceedWithFindingBlobs.bind(this);
        
        this.repeatPhoto = this.repeatPhoto.bind(this);
        this.usePhoto = this.usePhoto.bind(this);
    }

    // dummy() {
    //     return new Promise((resolve, reject) => {
    //         OpenCV.bark(error => {
    //             console.log('error', error);
    //         }, msg => {
    //             resolve(msg);
    //         });
    //     });    
    // }


    repeatPhoto() {
        this.setState({
            ...this.state,
            photoAsBase64: {
                content: '',
                isPhotoPreview: false,
                photoPath: '',
            },
        });
    }

    usePhoto() {
        // do something, e.g. navigate
    }

    async takePicture() {
        if (this.camera) {
            const options = { quality: 0.5, base64: true };
            const data = await this.camera.takePictureAsync(options);
            console.log('data', data);
            this.setState({
                ...this.state,
                photoAsBase64: { content: data.base64, isPhotoPreview: false, photoPath: data.uri },
            });
            this.proceedWithCheckingBlurryImage();
        }
    }
    
    proceedWithCheckingBlurryImage() {
        const { content, photoPath } = this.state.photoAsBase64;
        this.checkForBlurryImage(content).then(blurryPhoto => {
            if (blurryPhoto) {
                this.refs.toast.show('Photo is blurred!', DURATION.FOREVER);
                return this.repeatPhoto();
            }
            this.refs.toast.show('Photo is clear!', DURATION.FOREVER);
            this.setState({ photoAsBase64: { ...this.state.photoAsBase64, isPhotoPreview: true, photoPath } });
        }).catch(err => {
            console.log('err', err)
        });
    }

    checkForBlurryImage(imageAsBase64) {
        console.log('checking for blurry image');
        return new Promise((resolve, reject) => {
            if (Platform.OS === 'android') {
                OpenCV.checkForBlurryImage(imageAsBase64, error => {
                    // error handling
                    console.log('error', error);
                }, msg => {
                    resolve(msg);
                });
            } else if (Platform.OS === 'ios') {
                OpenCV.checkForBlurryImage(imageAsBase64, (error, dataArray) => {
                    resolve(dataArray[0]);
                });
            }
        });
    }

    proceedWithFindingBlobs() {
        const { content, photoPath } = this.state.photoAsBase64;
        this.findBlobs(content).then(blobsFound => {
            
            if (blobsFound) {
                this.refs.toast.show('blobs found', DURATION.FOREVER);
                return this.repeatPhoto();
            }
            this.refs.toast.show('blobs not found', DURATION.FOREVER);
            this.setState({ photoAsBase64: { ...this.state.photoAsBase64, isPhotoPreview: true, photoPath } });
        }).catch(err => {
            console.log('err', err)
        });
    }

    findBlobs() {
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
        // invoking the dummy function
        // this.dummy().then(msg => console.log('msg', msg));
        console.log('started rendering...');
        if(!this.state.photoAsBase64.content)
        console.log('this.state.photoAsBase64.content is currently empty');

        console.log('this.state.photoAsBase64.content', this.state.photoAsBase64.content);

        if (this.state.photoAsBase64.isPhotoPreview) {
            return (
                <View style={styles.container}>
                    <Toast ref="toast" position="center" />
                    <Image
                        source={{ uri: `data:image/png;base64,${this.state.photoAsBase64.content}` }}
                        style={styles.imagePreview}
                    />
                    <View style={styles.repeatPhotoContainer}>
                        <TouchableOpacity onPress={this.repeatPhoto}>
                            <Text style={styles.photoPreviewRepeatPhotoText}>
                                Repeat photo
              </Text>
                        </TouchableOpacity>
                    </View>
                    <View style={styles.usePhotoContainer}>
                        <TouchableOpacity onPress={this.usePhoto}>
                            <Text style={styles.photoPreviewUsePhotoText}>
                                Use photo
              </Text>
                        </TouchableOpacity>
                    </View>
                </View>
            );
        }

        return (
            <View style={styles.container}>
                <Camera
                    ref={cam => {
                        this.camera = cam;
                    }}
                    style={styles.preview}
                    permissionDialogTitle={'Permission to use camera'}
                    permissionDialogMessage={'We need your permission to use your camera phone'}
                >
                    <View style={styles.takePictureContainer}>
                        <TouchableOpacity onPress={this.takePicture}>
                            <View>
                                <CircleWithinCircle />
                            </View>
                        </TouchableOpacity>
                    </View>
                </Camera>
                <Toast ref="toast" position="center" />
            </View>
        );
    }
}

export default CameraTest;

const styles = StyleSheet.create({
    imagePreview: {
        position: 'absolute',
        top: 0,
        right: 0,
        left: 0,
        bottom: 60,
    },
    container: {
        flex: 1,
        flexDirection: 'row',
    },
    repeatPhotoContainer: {
        position: 'absolute',
        bottom: 0,
        left: 0,
        width: '50%',
        height: 120,
        backgroundColor: '#000',
        alignItems: 'flex-start',
        justifyContent: 'center',
    },
    topButtonsContainer: {
        flexDirection: 'row',
        alignItems: 'center',
        position: 'absolute',
        top: 0,
        left: 0,
        width: '100%',
        padding: 10,
        justifyContent: 'space-between',
    },
    focusFrameContainer: {
        position: 'absolute',
        top: 0,
        left: 0,
        height: '100%',
        width: '100%',
    },
    focusFrame: {
        height: 90,
        width: 90,
        borderWidth: 1,
        borderColor: '#fff',
        borderStyle: 'dotted',
        borderRadius: 5,
    },
    photoPreviewRepeatPhotoText: {
        color: '#abcfff',
        fontSize: 15,
        marginLeft: 10,
    },
    usePhotoContainer: {
        position: 'absolute',
        bottom: 0,
        right: 0,
        width: '50%',
        height: 120,
        backgroundColor: '#000',
        alignItems: 'flex-end',
        justifyContent: 'center',
    },
    photoPreviewUsePhotoText: {
        color: '#abcfff',
        fontSize: 15,
        marginRight: 10,
    },
    preview: {
        position: 'relative',
        flex: 1,
        justifyContent: 'flex-end',
        alignItems: 'center',
    },
    takePictureContainer: {
        position: 'absolute',
        paddingVertical: 20,
        bottom: 0,
        left: 0,
        right: 0,
        justifyContent: 'center',
        alignItems: 'center',
    },
});
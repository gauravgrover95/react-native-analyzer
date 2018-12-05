/** @format */

import {AppRegistry} from 'react-native';
// import App from './App';
import CameraTest from './CameraTest'; 
import ImagePickerTest from './ImagePickerTest';
import {name as appName} from './app.json';

// AppRegistry.registerComponent(appName, () => App);
// AppRegistry.registerComponent(appName, () => CameraTest);
AppRegistry.registerComponent(appName, () => ImagePickerTest);

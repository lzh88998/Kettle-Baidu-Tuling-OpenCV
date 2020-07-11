# Kettle-Baidu-Tuling-OpenCV

This project contains several Kettle Steps which can be used on Raspberry PI:
1. Recorder
When use with Raspberry PI, it can record from microphone, user can select a microphone device and set a threshold for speaking detection. Voice stream from microphone can be converted to a Kettle binary data stream and used by Speaker or Baidu ASR steps.
2. BaiduASR
Can post voice stream recorded from Recorder step to Baidu webservice API to run speach recognition, return recognized text in string data type. Detailed API documentation can be found at http://ai.baidu.com/.
3. Tuling
Can post String type information to Tuling chatbot webservice API and get response text for given sentence. For details about Tuling chatbot please visit: http://www.turingapi.com/
4. BaiduTTS
Convert text to a voice speech binary stream which can be used by Speaker step.
5. Speaker
Playback voice using speakers
6. OpenCV
When use Raspberry PI, this step can capture human faces,eyes etc by using an OpenCV model then output the location of the captured item as coordinates on the screen. for OpenCV documentation please visit http://www.opencv.org

Below items are used for Raspberry PI sensor hat. 
Special thanks for https://github.com/cinci/rpi-sense-hat-java

7. Sensor
Get data from Raspberry PI sensors
8. LedMatrix
Control Raspberry PI LED matrix

# ThreadVoice

ThreadVoice is a Twitter bot that turns Twitter threads into listenable audio podcasts. The Text-to-Speech engine is powered by Google, Microsoft, Amazon and VoiceRSS.

## Setup

Make sure to install the Java JDK, Maven and MYSQL on your server if you don't have them already.  

If you are on Ubuntu, run the following codes to install Java, Maven and MYSQL.

```
sudo apt update -y
sudo apt install default-jdk -y
sudo apt install maven -y
sudo apt install mysql-server -y
```

Replace `src/main/resources/settings.properties.example` with `src/main/resources/settings.properties`.  
`cp src/main/resources/settings.properties.example src/main/resources/settings.properties`    

Replace `src/main/resources/gcloudkey.json.example` with `src/main/resources/gcloudkey.json`.  
`cp src/main/resources/gcloudkey.json.example src/main/resources/gcloudkey.json`  

Credential settings are stored inside the `src/main/resources/settings.properties` file.
Edit the `settings.properties` file.  

### Storage location of audio files
The `voice.path` is the location where the audio files are stored on the server. The default is set to `/var/www/html/audio/` which is inside the apache public `www/html/` directory for access on port 80.
`voice.path=`

### MYSQL
Setup your MYSQL server and create a database named `threadvoice`.  
Import the `src/main/resources/schema.sql` SQL file to create the appropriate database tables on your MYSQL server.  

Replace `user=root&password=12345` under the `mysql.db` properties with your correct MYSQL username and password.  

### Twitter API Keys

Replace the value of `twitter.username=` property with your Twitter bot username. Make sure to include the `@` before it,

Replace the value of the following properties with your Twitter Developer API credentials (https://developer.twitter.com/en)
```
tw.apiKey=
tw.apiSecretKey=
tw.accessToken=
tw.accessTokenSecret=
```

### TTS API Engine Setup

#### Gcloud
To setup Google Cloud Text To Speech, create a Google Cloud Text to Speech project. Follow the instructions here generate your GCloud API credentials json file https://cloud.google.com/docs/authentication/getting-started#create-service-account-console.  
Copy the contents of the generated GCloud json file and insert it inside the `src/main/resources/gcloudkey.json` file. Overwrite the existing file contents with it.  
Google Cloud Text-to-Speech: https://cloud.google.com/text-to-speech

#### Amazon Polly Keys
Replace the value of below properties with your own Amazon Polly API keys.  
```
aws.accesskey=
aws.secretkey=
```
Amazon Polly: https://aws.amazon.com/polly

#### Microsoft Text
Replace the value of below property with your own Microsoft TTS API key.  
`microsoft.key=`  

Microsoft Text To Speech: https://azure.microsoft.com/en-us/services/cognitive-services/text-to-speech/  

#### Microsoft Text
Replace the value of below property with your own VoiceRSS API key.  
`voicerss.key=` 

VoiceRSS: https://www.voicerss.org/  

#### To install the VoiceRSS library
The VoiceRSS Java library is not available on maven central. The jar file is stored inside the `libs` folder.  
Execute the below code on the main directory of this project.  

```
    mvn install:install-file \
   -Dfile=libs/voicerss_tts.jar \
   -DgroupId=com.voicerss.tts \
   -DartifactId=voicerss \
   -Dversion=1.0 \
   -Dpackaging=jar \
   -DgeneratePom=true
```

#### To build and compile the program
`mvn clean install`

After a successful build, you can now start the program with:  
`java -jar target/threadvoice-2.0-SNAPSHOT.jar`  
Use `nohup` to keep the program running even after the terminal is closed:  
`nohup java -jar target/threadvoice-2.0-SNAPSHOT.jar &`  


#### TTS Limits
Each Text-To-Speech provider have their character limit per request.

| Provider  | Character Limits               |
|:----------|:-------------------------------|
| Google    | 5000                           |
| Amazon    | 3000                           |
| Microsoft | 10000; max audio is 10 minutes |
| Voice RSS | 100KB                          |

## LICENSE
MIT License

Copyright (c) 2023 Philip Okugbe

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

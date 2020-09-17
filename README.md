# Dialogflow Navigation
Let's create an Android app where you navigate with your voice.  
**It's based on [abhi007tyagi's library](https://github.com/abhi007tyagi/Android_Dialogflow_Chatbot_Library).**

## What you have to do
1. Fork this project
2. Add you service_account.json file in the [res/raw](./app/src/main/res/raw) application folder. This json should give you accees to your Dialogflow project as API Client. To create the json file, take a look at [this medium blog post written by @abhi007tyagi](https://medium.com/@abhi007tyagi/android-chatbot-with-dialogflow-8c0dcc8d8018).
3. Add you intent in the MainActivity -> [processDFResponse method](https://github.com/StephaneC/DialogflowNavigation/blob/3b06659243c480393ac198880bc50decb04e7975/app/src/main/java/com/castrec/stephane/dialogflownavigation/MainActivity.kt#L116)

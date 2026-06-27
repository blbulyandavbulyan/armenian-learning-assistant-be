# armenian-learning-assistant-be
This is the backend for Armenian language learning assistant.

Tech stack:
* Spring Boot 4
* Java 25
* Spring AI
* python3 + piper (for text to speech)
* Gemini model for phrases generation

## MCP servers
There is github mcp server, if you want to use it, you have to create .env file in .agents folder with your personal access token, like this:
```
GITHUB_PERSONAL_ACCESS_TOKEN=YOUR_TOKEN
```

## Running locally
Make sure you have maven installed.
You also need python (you should follow the instructions below to set up piper)
When you run the application locally you have to provide:
* `local` as your Spring Boot Profile
* `GEMINI_API_KEY` as environment variable, with your gemini api key

### 🎙️ Text-to-Speech (TTS) Setup & Troubleshooting
```bash
# 1. Initialize the virtual environment
python3 -m venv myenv

# 3. Activate the environment
source myenv/bin/activate

# 4. Install the Piper TTS binary wrapper
pip install piper-tts
pip install 'piper-tts[http]'

# Download the Neural Weights
wget https://huggingface.co/davit312/piper-TTS-Armenian/resolve/main/v3/hy_AM-gor-medium.onnx -O hy_AM-medium.onnx

# Download the Structure Config Map
wget https://huggingface.co/davit312/piper-TTS-Armenian/resolve/main/v3/hy_AM-gor-medium.onnx.json -O hy_AM-medium.onnx.json

# To test your setup
python3 text-to-speech-test.py

# Run the piper API server locally
python3 -m piper.http_server -m hy_AM-medium.onnx
```

#### Running the Piper API server locally
This is essential step in the setup

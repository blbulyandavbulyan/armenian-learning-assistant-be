## 🎙️ Text-to-Speech (TTS) Setup & Troubleshooting
```bash
# 1. Initialize the virtual environment
python3 -m venv myenv

# 3. Activate the environment
source myenv/bin/activate

# 4. Install the Piper TTS binary wrapper
pip install piper-tts

# Download the Neural Weights
wget https://huggingface.co/davit312/piper-TTS-Armenian/resolve/main/v3/hy_AM-gor-medium.onnx -O hy_AM-medium.onnx

# Download the Structure Config Map
wget https://huggingface.co/davit312/piper-TTS-Armenian/resolve/main/v3/hy_AM-gor-medium.onnx.json -O hy_AM-medium.onnx.json
```

Then Run `text-to-speech-test.py`:
```bash
python3 text-to-speech-test.py
```

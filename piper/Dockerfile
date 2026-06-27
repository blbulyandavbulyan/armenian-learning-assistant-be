FROM python:3.10-slim

WORKDIR /app

RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/* && \
    pip install --no-cache-dir piper-tts "piper-tts[http]" && \
    wget https://huggingface.co/davit312/piper-TTS-Armenian/resolve/main/v3/hy_AM-gor-medium.onnx -O hy_AM-medium.onnx && \
    wget https://huggingface.co/davit312/piper-TTS-Armenian/resolve/main/v3/hy_AM-gor-medium.onnx.json -O hy_AM-medium.onnx.json && \
    groupadd -r piper && useradd -r -g piper piper

USER piper

EXPOSE 5000

CMD ["python", "-m", "piper.http_server", "-m", "hy_AM-medium.onnx", "--port", "5000"]

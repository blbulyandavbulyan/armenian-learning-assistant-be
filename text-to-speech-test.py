import subprocess

# Define your text and local model paths
text_to_speak = "Որտե՞ղ է հացի բաժինը:"
model_path = "./hy_AM-medium.onnx"
config_path = "./hy_AM-medium.onnx.json"
output_file = "welcome.wav"

# Construct the command exactly as Piper expects it
command = [
    "piper",
    "--model", model_path,
    "--output_file", output_file,
    "--config", config_path
]

print(True, f"Feeding text to Piper: {text_to_speak}")

# Run Piper and pass the text directly into stdin via Python's robust string handling
process = subprocess.Popen(command, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, encoding='utf-8')
stdout, stderr = process.communicate(input=text_to_speak)

if process.returncode == 0:
    print(f"Success! Generated file saved as {output_file}")
else:
    print(f"Error occurred:\n{stderr}")
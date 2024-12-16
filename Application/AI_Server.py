import subprocess
import numpy as np
import tensorflow as tf
from tensorflow.keras import layers
from flask import Flask, request, jsonify
from transformers import DistilBertTokenizer, TFDistilBertModel
import librosa
import os
import time
import random

os.environ["PATH"] += os.pathsep + r"C:/ffmpeg-master-latest-win64-gpl/bin"

app = Flask(__name__)

# Đảm bảo thư mục lưu trữ tệp đã tồn tại
UPLOAD_FOLDER = 'dataset/DAIC_WOZ/dataset_2/new_dataset_7_6_s/12_03/upload'
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

# Cấu hình mô hình AI
class AdditiveAttention(layers.Layer):
    def __init__(self, **kwargs):
        super(AdditiveAttention, self).__init__(**kwargs)
        self.attention_dense = layers.Dense(1, activation="tanh", kernel_regularizer=tf.keras.regularizers.l2(0.01))

    def call(self, inputs):
        attention = self.attention_dense(inputs)
        attention_weights = tf.nn.softmax(attention, axis=1)
        context_vector = tf.reduce_sum(inputs * attention_weights, axis=1)
        return context_vector

model = tf.keras.models.load_model('dataset/DAIC_WOZ/dataset_2/new_dataset_7_6_s/12_03/model.h5', custom_objects={'AdditiveAttention': AdditiveAttention})
tokenizer = DistilBertTokenizer.from_pretrained("distilbert-base-uncased")
distilbert_model = TFDistilBertModel.from_pretrained("distilbert-base-uncased")

# Hàm chuyển MP4 thành WAV
def convert_mp4_to_wav(mp4_file_path, wav_file_path):
    ffmpeg_path = "ffmpeg"  # Đảm bảo rằng FFmpeg đã được cài đặt và có trong PATH
    command = [ffmpeg_path, '-i', mp4_file_path, '-ac', '1', '-ar', '16000', wav_file_path]
    subprocess.run(command, check=True)

# Hàm xử lý dữ liệu văn bản
def encode_full_text(text, max_len=46):
    inputs = tokenizer(text, padding='max_length', truncation=True, max_length=max_len, return_tensors="tf")
    outputs = distilbert_model(inputs["input_ids"])
    return outputs.last_hidden_state  # Trả về (1, 46, 768)

# Hàm để tính MFCC từ file WAV
def extract_mfcc(audio_path, n_mfcc=13, sr=16000, target_length=469):
    y, sr = librosa.load(audio_path, sr=sr)
    mfcc = librosa.feature.mfcc(y=y, sr=sr, n_mfcc=n_mfcc)
    segments = []
    total_frames = mfcc.shape[1]
    for start_idx in range(0, total_frames, target_length):
        end_idx = min(start_idx + target_length, total_frames)
        segment = mfcc[:, start_idx:end_idx]
        if segment.shape[1] < target_length:
            pad_width = target_length - segment.shape[1]
            segment = np.pad(segment, ((0, 0), (0, pad_width)), mode='constant')
        segments.append(segment)
    return np.array(segments).transpose(0, 2, 1)

# Hàm dự đoán từ mô hình AI
def predict_input(text_input=None, audio_file_path=None):
    # Mã hóa văn bản nếu có
    text_tensor = encode_full_text(text_input) if text_input else np.zeros((1, 46, 768))

    # Nếu có âm thanh, trích xuất MFCC và xử lý từng đoạn
    if audio_file_path:
        # Trích xuất các đoạn MFCC từ tệp âm thanh
        segments = extract_mfcc(audio_file_path)

        # Khởi tạo mảng để lưu kết quả dự đoán cho từng đoạn
        all_predictions = []

        # Dự đoán cho từng đoạn
        for segment in segments:
            # Đảm bảo rằng segment có kích thước phù hợp cho mô hình (1, target_length, n_mfcc)
            segment_tensor = segment.reshape(1, 469, 13)  # Chỉnh lại kích thước nếu cần

            # Dự đoán cho đoạn này
            prediction = model.predict([segment_tensor, text_tensor])
            all_predictions.append(prediction)

        # Tính trung bình của tất cả các dự đoán
        final_prediction = np.mean(all_predictions, axis=0)
    else:
        # Nếu không có âm thanh, tạo tensor rỗng
        audio_tensor = np.zeros((1, 469, 13))  
        final_prediction = model.predict([audio_tensor, text_tensor])

    return final_prediction


# Route gốc - trả về thông điệp khi truy cập vào '/'
@app.route('/', methods=['GET'])
def index():
    return jsonify({'message': 'Welcome to the audio upload server! Use /upload to POST audio files.'})

# Endpoint nhận tệp và thực hiện dự đoán
@app.route('/upload', methods=['POST'])
def upload_audio():
    if 'file' not in request.files and 'text' not in request.form:
        return jsonify({'status': 'error', 'message': 'No file or text uploaded'}), 400

    file = request.files.get('file')
    text_input = request.form.get('text')

    if file:
        # Lấy tên tệp gốc
        mp4_filename = file.filename

        # Tạo một tên tệp mới dựa trên thời gian hiện tại và một số ngẫu nhiên để tránh trùng lặp
        timestamp = int(time.time())  # Lấy thời gian hiện tại tính theo giây
        random_number = random.randint(1000, 9999)  # Số ngẫu nhiên
        new_filename = f"{timestamp}_{random_number}_{mp4_filename}"

        # Đặt đường dẫn mới để lưu tệp
        mp4_filepath = os.path.join(UPLOAD_FOLDER, new_filename)

        # Lưu tệp vào thư mục
        file.save(mp4_filepath)

        # Chuyển đổi MP4 thành WAV
        wav_filename = new_filename.replace(".mp4", ".wav")
        wav_filepath = os.path.join(UPLOAD_FOLDER, wav_filename)
        convert_mp4_to_wav(mp4_filepath, wav_filepath)

        # Dự đoán kết quả từ âm thanh
        prediction = predict_input(audio_file_path=wav_filepath)

        # Trả về kết quả dự đoán
        return jsonify({'prediction': float(prediction)}), 200
    
    elif text_input:
        # Dự đoán kết quả từ văn bản
        prediction = predict_input(text_input=text_input)
        return jsonify({'prediction': float(prediction[0][0])}), 200

    else:
        return jsonify({'status': 'error', 'message': 'No valid input provided'}), 400

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
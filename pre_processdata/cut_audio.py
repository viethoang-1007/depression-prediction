import os
import pandas as pd
from pydub import AudioSegment

# Đường dẫn đến thư mục chứa file WAV và CSV
audio_folder = 'dataset/DAIC_WOZ/dataset_2/audio'  # Thay bằng đường dẫn thư mục WAV
csv_folder = 'dataset/DAIC_WOZ/dataset_2/new_dataset_7_6_s/cut_transcript'      # Thay bằng đường dẫn thư mục CSV
output_folder = 'dataset/DAIC_WOZ/dataset_2/new_dataset_7_6_s/cut_audio_segment'  # Thư mục xuất file WAV sau khi cắt


# Tạo thư mục output nếu chưa tồn tại
os.makedirs(output_folder, exist_ok=True)

def cut_and_merge_audio(audio_file, csv_file, output_file):
    # Đọc file CSV
    df = pd.read_csv(csv_file)
    
    # Lọc chỉ lấy các dòng có speaker là 'Participant'
    df = df[df['speaker'] == 'Participant']
    
    # Load file audio
    audio = AudioSegment.from_wav(audio_file)
    
    # Biến chứa audio đã cắt
    merged_audio = AudioSegment.empty()
    
    # Duyệt qua từng hàng trong file CSV
    for index, row in df.iterrows():
        start_time = int(row['start_time'] * 1000)  # chuyển giây thành ms
        stop_time = int(row['stop_time'] * 1000)   # chuyển giây thành ms
        segment = audio[start_time:(stop_time+200)]
        merged_audio += segment  # Ghép đoạn này vào đoạn tổng
    
    # Lưu file audio đã cắt và ghép
    merged_audio.export(output_file, format="wav")
    print(f"Exported merged file: {output_file}")

# Duyệt qua từng file audio và file CSV tương ứng
for file in os.listdir(audio_folder):
    if file.endswith('_AUDIO.wav'):
        base_number = file.split('_')[0]
        audio_path = os.path.join(audio_folder, file)
        
        # Tìm các file CSV tương ứng
        csv_files = [f for f in os.listdir(csv_folder) if f.startswith(base_number) and f.endswith('.csv')]
        
        for csv_file in csv_files:
            csv_path = os.path.join(csv_folder, csv_file)
            output_file = os.path.join(output_folder, f"{base_number}_{os.path.splitext(csv_file)[0].split('_')[-1]}.wav")
            
            # Thực hiện cắt và ghép audio
            cut_and_merge_audio(audio_path, csv_path, output_file)


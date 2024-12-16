import os
import pandas as pd

def process_transcript_file(input_file, final_output_directory, max_duration=10000):
    # Đọc file đầu vào
    data = pd.read_csv(input_file)

    file_name = os.path.basename(input_file).split('_')[0]  # Trích xuất tên file để dùng trong file đầu ra
    file_counter = 1
    start_index = 0
    merged_data = pd.DataFrame()
    total_duration = 0

    for i in range(1, len(data)):
        # Kiểm tra nếu người nói là 'Participant'
        if data.loc[i, 'speaker'] == 'Participant' or i == len(data) - 1:
            # Lấy đoạn từ start_index đến hàng hiện tại
            split_data = data.iloc[start_index:i+1]

            # Tính thời lượng cho speaker 'Participant'
            participant_data = split_data[split_data['speaker'] == 'Participant']
            participant_data['stop_time'] = pd.to_numeric(participant_data['stop_time'], errors='coerce')
            participant_data['start_time'] = pd.to_numeric(participant_data['start_time'], errors='coerce')

            if participant_data['stop_time'].isna().any() or participant_data['start_time'].isna().any():
                print(f"Invalid time data in split segment {file_counter} of {file_name}. Skipping this segment...")
                start_index = i + 1
                continue

            file_duration = (participant_data['stop_time'].sum() - participant_data['start_time'].sum()) * 1000

            # Kiểm tra điều kiện ghép file
            if total_duration + file_duration > max_duration:
                # Lưu dữ liệu đã ghép nếu vượt max_duration
                if not merged_data.empty:
                    output_file_name = os.path.join(final_output_directory, f"{file_name}_merged_file_{file_counter}.csv")
                    merged_data.to_csv(output_file_name, index=False)
                    print(f"Saved merged file: {output_file_name}")
                    file_counter += 1

                # Reset dữ liệu đã ghép
                merged_data = split_data
                total_duration = file_duration
            else:
                # Thêm dữ liệu vào merged_data
                merged_data = pd.concat([merged_data, split_data], ignore_index=True)
                total_duration += file_duration

            # Cập nhật start_index cho đoạn tiếp theo
            start_index = i + 1

    # Lưu file cuối cùng nếu còn dữ liệu
    if not merged_data.empty:
        output_file_name = os.path.join(final_output_directory, f"{file_name}_merged_file_{file_counter}.csv")
        merged_data.to_csv(output_file_name, index=False)
        print(f"Saved final merged file: {output_file_name}")


def process_transcript_folder(input_directory, final_output_directory, max_duration=10000):
    os.makedirs(final_output_directory, exist_ok=True)  # Tạo thư mục đầu ra nếu chưa tồn tại

    # Lấy danh sách tất cả các file CSV trong thư mục đầu vào
    files = [os.path.join(input_directory, f) for f in os.listdir(input_directory) if f.endswith("_TRANSCRIPT.csv")]

    print(f"Found {len(files)} files to process.")
    
    for file in files:
        print(f"Processing file: {file}")
        process_transcript_file(file, final_output_directory, max_duration)

    print(f"All files processed and saved to {final_output_directory}")


# Ví dụ sử dụng
input_directory = "dataset/DAIC_WOZ/dataset_2/new_dataset_7_6_s/revise_transcript_7_6"  # Thư mục đầu vào chứa các file CSV
final_output_directory = "dataset/DAIC_WOZ/dataset_2/new_dataset_7_6_s/cut_transcript"  # Thư mục đầu ra lưu các file cuối cùng

# Chạy chương trình
process_transcript_folder(input_directory, final_output_directory, max_duration=10000)

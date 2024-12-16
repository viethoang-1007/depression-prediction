import pandas as pd
import re
from nltk.corpus import stopwords
from nltk.stem import WordNetLemmatizer, SnowballStemmer

# Load the dataset
file_path = 'dataset/DAIC_WOZ/dataset_2/new_dataset_7_6_s/12_03/train_transcripts.csv'
df = pd.read_csv(file_path)

# Define the text preprocessing function
def text_to_wordlist(text, remove_stopwords=True, stem_words=False):    
    # Ensure that the text is a string
    if not isinstance(text, str):
        return ""  # Return empty string if text is not a string (e.g., NaN or float)

    # Clean the text, with the option to remove stopwords and to stem words.
    
    # Convert words to lower case and split them
    text = text.lower().split()

    # Optionally, remove stop words
    if remove_stopwords:
        stops = set(stopwords.words("english"))
        text = [WordNetLemmatizer().lemmatize(w) for w in text if not w in stops ]
        text = [w for w in text if w != "nan"]
    
    text = " ".join(text)

    # Clean the text
    text = re.sub(r"[^A-Za-z0-9^,!.\/'+-=]", " ", text)
    text = re.sub(r"what's", "what is ", text)
    text = re.sub(r"\'s", " ", text)
    text = re.sub(r"\'ve", " have ", text)
    text = re.sub(r"can't", "cannot ", text)
    text = re.sub(r"n't", " not ", text)
    text = re.sub(r"i'm", "i am ", text)
    text = re.sub(r"\'re", " are ", text)
    text = re.sub(r"\'d", " would ", text)
    text = re.sub(r"\'ll", " will ", text)
    text = re.sub(r",", " ", text)
    text = re.sub(r"\.", " ", text)
    text = re.sub(r"!", " ! ", text)
    text = re.sub(r"\/", " ", text)
    text = re.sub(r"\^", " ^ ", text)
    text = re.sub(r"\+", " + ", text)
    text = re.sub(r"\-", " - ", text)
    text = re.sub(r"\=", " = ", text)
    text = re.sub(r"\<", " ", text)
    text = re.sub(r"\>", " ", text)
    text = re.sub(r"'", " ", text)
    text = re.sub(r"(\d+)(k)", r"\g<1>000", text)
    text = re.sub(r":", " : ", text)
    text = re.sub(r" e g ", " eg ", text)
    text = re.sub(r" b g ", " bg ", text)
    text = re.sub(r" u s ", " american ", text)
    text = re.sub(r"\0s", "0", text)
    text = re.sub(r" 9 11 ", "911", text)
    text = re.sub(r"e - mail", "email", text)
    text = re.sub(r"j k", "jk", text)
    text = re.sub(r"\s{2,}", " ", text)
    
    # Optionally, shorten words to their stems
    if stem_words:
        text = text.split()
        stemmer = SnowballStemmer('english')
        stemmed_words = [stemmer.stem(word) for word in text]
        text = " ".join(stemmed_words)
    
    # Return the processed text
    return text

# Apply the preprocessing function to the 'Text' column
df['Processed_Text'] = df['Text'].apply(lambda x: text_to_wordlist(x))

# Save the result to a new file
processed_file_path = 'dataset/DAIC_WOZ/dataset_2/new_dataset_7_6_s/12_03/processed_train_transcripts.csv'
df.to_csv(processed_file_path, index=False)

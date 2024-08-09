"""
# File       :commonUtils.py
# version    :python 3.9
# Description：
"""
import os
import shutil


def cp_all_jar_files(tgt_dir:str, src_dir:str):
    """
    Copy all jar files from src_dir to tgt_dir
    :param tgt_dir: target directory
    :param src_dir: source directory
    :return: None
    """
    for root, dirs, files in os.walk(src_dir):
        for file in files:
            if file.endswith('.jar') and not file.endswith('sources.jar') and "test" not in file:
                src_file = os.path.join(root, file)
                tgt_file = os.path.join(tgt_dir, file)
                if os.path.exists(tgt_file):
                    continue
                shutil.copy(src_file, tgt_file)
    print(f"Copy all jar files from {src_dir} to {tgt_dir} successfully!")


def cp_all_proto_files(tgt_dir:str, src_dir:str):
    """
    Copy all proto files from src_dir to tgt_dir
    :param tgt_dir: target directory
    :param src_dir: source directory
    :return: None
    """
    for root, dirs, files in os.walk(src_dir):
        for file in files:
            if file.endswith('.proto'):
                src_file = os.path.join(root, file)
                tgt_file = os.path.join(tgt_dir, file)
                if os.path.exists(tgt_file):
                    continue
                shutil.copy(src_file, tgt_file)
    print(f"Copy all proto files from {src_dir} to {tgt_dir} successfully!")


def split_camel_word(camel_word: str)->list:
    """
    Split camel word to separate words, handling consecutive uppercase letters as a single word.
    :param camel_word: camel word
    :return: list of separate words
    """
    words = []
    word = ""
    for i, char in enumerate(camel_word):
        if char.isupper():
            if word and (not word[-1].isupper() or (i + 1 < len(camel_word) and not camel_word[i + 1].isupper())):
                words.append(word)
                word = char
            else:
                word += char
        else:
            word += char
    words.append(word)
    return words


def format_impl_class_mtd_sig(impl_class:str,  gRPC_func_name:str, all_edge:list):
    '''
    Format the impl API method signature
    :param impl_class:
    :param gRPC_func_name:
    :param all_edge:
    :return:
    '''
    tgt_sub_sig = impl_class + ":" + gRPC_func_name
    for edge in all_edge:
        for mtd in edge:
            if tgt_sub_sig in mtd:
                return mtd
    return ""





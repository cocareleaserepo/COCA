"""
# File       :envUtils.py
# version    :python 3.9
# Description：
"""
import os
from utils.commonUtils import cp_all_jar_files, cp_all_proto_files
from buildCG import build_all_cg
from utils.gRPCUtils import compile_proto_files


def prepare_work(binary_project_dir, source_project_dir):
    '''
    Prepare folds and files for analysis
    :param binary_project_dir:
    :param source_project_dir:
    :return:
    '''
    prepare_dir()
    cp_work_dir(binary_project_dir, source_project_dir)


def prepare_dir():
    '''
    Prepare target folders
    :return:
    '''
    if not os.path.exists("./tmp"):
        os.mkdir("./tmp")
    if not os.path.exists("./all_call_graph"):
        os.mkdir("./all_call_graph")
    if not os.path.exists("./all_jar_files"):
        os.mkdir("./all_jar_files")
    if not os.path.exists("./API"):
        os.mkdir("API")
    if not os.path.exists("all_proto_desc"):
        os.mkdir("all_proto_desc")
    if not os.path.exists("./all_proto_text"):
        os.mkdir("./all_proto_text")


def cp_work_dir(binary_project_dir, source_project_dir):
    '''
    Copy files to corresponding folders
    :param binary_project_dir:
    :param source_project_dir:
    :return:
    '''
    cp_all_jar_files("./all_jar_files", binary_project_dir)
    cp_all_proto_files("./all_proto_text", source_project_dir)
    build_all_cg("./all_jar_files", "./tmp", "./all_call_graph")
    compile_proto_files("./all_proto_text", "all_proto_desc")

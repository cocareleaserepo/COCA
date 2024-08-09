"""
# File       :gRPCUtils.py
# version    :python 3.9
# Description：
"""

import os
from gRPCfunc import gRPCfunc
from utils.textUtils import get_call_edge_common_signature
from google.protobuf import descriptor_pb2
import re


def compile_proto_files(project_dir:str, proto_path:str):
    '''
    Compile the .proto file to binary
    :param project_dir:
    :param proto_path:
    :return:
    '''
    all_proto_files = get_all_proto_files(project_dir)
    for proto_file in all_proto_files:
        proto_file_name = os.path.basename(proto_file)
        compiled_proto_file_name = os.path.join(proto_path, proto_file_name.replace(".proto", ".desc"))
        cmd = "protoc -I={} --descriptor_set_out={} {}".format(project_dir, compiled_proto_file_name, proto_file)
        os.system(cmd)


def get_gRPC_func_list(desc_dir:str)->list:
    """
    Get all gRPC functions in the project
    :param project_dir: project directory
    :return: a list of gRPC functions
    """
    all_proto_files = os.listdir(desc_dir)
    all_gRPC_func_list = []
    for proto_file in all_proto_files:
        gRPC_func_list = get_gRPC_func_list_from_proto(os.path.join(desc_dir, proto_file))
        all_gRPC_func_list.extend(gRPC_func_list)
    return all_gRPC_func_list


def get_all_proto_files(project_dir:str)->list:
    """
    Get all proto files in the project
    :param project_dir: project directory
    :return: a list of proto files
    """
    all_proto_files = []
    for root, dirs, files in os.walk(project_dir):
        for file in files:
            if file.endswith('.proto'):
                all_proto_files.append(os.path.join(root, file))
    return all_proto_files


def get_gRPC_func_list_from_proto(proto_file:str)->list:
    '''
    Get all gRPC functions from the proto file
    :param proto_file:
    :return:
    '''
    gRPCfunc_list = []
    with open(proto_file, "rb") as f:
        file_descriptor_set = descriptor_pb2.FileDescriptorSet()
        file_descriptor_set.ParseFromString(f.read())

    for file_descriptor_proto in file_descriptor_set.file:
        options = file_descriptor_proto.options
        package_name = options.java_package if options.HasField("java_package") else ""
        java_outer_classname = options.java_outer_classname if options.HasField("java_outer_classname") else ""

        for service_descriptor_proto in file_descriptor_proto.service:
            service_name = service_descriptor_proto.name

            for method_descriptor_proto in service_descriptor_proto.method:
                method_name = method_descriptor_proto.name
                input_type = method_descriptor_proto.input_type.strip(".")
                output_type = method_descriptor_proto.output_type.strip(".")
                grpc_func = gRPCfunc(package_name, java_outer_classname, service_name, method_name, input_type, output_type, proto_file)
                gRPCfunc_list.append(grpc_func)
    return gRPCfunc_list


def select_gRPC_call_site(all_edge:list, gRPC_func_list:list)->dict:
    grpc_cs_dict = {}
    for func in gRPC_func_list:
        gRPC_normal_signature = func.get_func_signature()
        edge_lst = []
        for edge in all_edge:
            edge_normal_siganature = get_call_edge_common_signature(edge[-1])
            if edge_normal_siganature == gRPC_normal_signature:
                edge_lst.append(edge)
        grpc_cs_dict[gRPC_normal_signature] = (func, edge_lst)
    return grpc_cs_dict
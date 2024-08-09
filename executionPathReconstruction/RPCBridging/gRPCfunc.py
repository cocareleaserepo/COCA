"""
# File       :gRPCfunc.py
# version    :python 3.9
# Description：
"""

class gRPCfunc(object):
    def __init__(self, package_name:str, clz_name:str, service_name:str, func_name:str, para_list:str, return_type:str, proto_file:str):
        self.package_name = package_name
        self.outer_class_name = clz_name
        self.service_name = service_name
        self.func_name = func_name
        self.para_list = para_list
        self.return_type = return_type
        self.proto_file = proto_file

    def get_func_signature(self)->str:
        return self.outer_class_name + "@" + self.func_name

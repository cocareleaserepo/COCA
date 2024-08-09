"""
# File       :textUtils.py
# version    :python 3.9
# Description：
"""


def get_call_edge_common_signature(call_edge_str:str):
    '''
    Transform the call edge signature to a unified form
    :param call_edge_str:
    :return:
    '''
    class_name = call_edge_str.split(':')[0].split(".")[-1]
    method_name = call_edge_str.split(":")[1].split("(")[0]
    return class_name + "@" + method_name


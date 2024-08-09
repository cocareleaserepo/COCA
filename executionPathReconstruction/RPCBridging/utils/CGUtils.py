"""
# File       :CGUtils.py
# version    :python 3.9
# Description：
"""

import os
from gRPCfunc import gRPCfunc
from utils.textUtils import get_call_edge_common_signature


def get_all_edges(CG_path:str)->set:
    '''
    Read all CG edge under the CG_path
    :param CG_path:
    :return:
    '''
    CG_file_list = os.listdir(CG_path)
    CG_list = []
    for file in CG_file_list:
        CG_list.append(get_call_graph(os.path.join(CG_path, file)))
    all_edges_set = set()
    for CG_edge_set in CG_list:
        all_edges_set = all_edges_set.union(CG_edge_set)
    return all_edges_set


def get_call_graph(CG_file_path:str)->set:
    '''
    Read CG edge files from CG file
    :param CG_file_path:
    :return:
    '''
    with open(CG_file_path, "r") as f:
        lines = f.readlines()
    edge_set = set()
    for line in lines:
        if line[:2] != "M:":
            continue

        caller = line.split(" ")[0].lstrip("M:").rstrip("\n")
        callee = line.split(" ")[1][3:].strip().rstrip("\n")
        if caller.startswith("java.") or callee.startswith("javax."):
            continue

        edge_set.add((caller, callee))
    return edge_set


def collect_all_mtd_sig(src_dir:str, tgt_dir:str)->list:
    '''
    Collect all method signature of the project
    :param src_dir:
    :param tgt_dir:
    :return:
    '''
    os.system("java -jar ./lib/becl_get_sig.jar " + src_dir)
    API_sig_lst = []
    for sig_file in os.listdir(tgt_dir):
        with open(os.path.join(tgt_dir, sig_file), "r") as f:
            for line in f.readlines():
                API_sig_lst.append(line.strip())
    return API_sig_lst


def update_cg_edge(service_impl_sig:str, gRPC_func:gRPCfunc, all_edge:list)->list:
    """
    Update the call graph edge
    :param ori_edge: original edge
    :param tgt_edge: target edge
    :param all_edge: all edges
    :return: None
    """
    gRPC_normal_signature = gRPC_func.get_func_signature()
    new_edge_list = []
    for edge in all_edge:
        edge_normal_siganature = get_call_edge_common_signature(edge[-1])
        if edge_normal_siganature == gRPC_normal_signature:
            new_edge = (edge[0], service_impl_sig)
            new_edge_list.append(new_edge)
        else:
            new_edge_list.append(edge)
    return new_edge_list


def write_call_graph(all_edge:list, tgt_file:str):
    '''
    Write the edge into specified file in text format
    :param all_edge:
    :param tgt_file:
    :return:
    '''
    with open(tgt_file, "w") as f:
        for edge in all_edge:
            f.write(edge[0] + " -> " + edge[1] + "\n")


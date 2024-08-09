"""
# File       :main.py
# version    :python 3.9
# Description：
"""
from gRPCfunc import gRPCfunc
from utils.envUtils import prepare_work
from utils.commonUtils import format_impl_class_mtd_sig
from utils.CGUtils import  get_all_edges, update_cg_edge, write_call_graph, collect_all_mtd_sig
from utils.gRPCUtils import get_gRPC_func_list
from fuzzyMatch import search_service_impl
import argparse


jar_dir = "./all_jar_files"
API_dir = "./API"
proto_desc_dir = "./all_proto_desc"
call_graph_dir = "./all_call_graph"

service_impl_dict = {}


def format_service_impl_sig(func_obj:gRPCfunc, all_mtd_sig:list, source_project_dir:str, all_edge:list)->str:
    '''
    Fetch the service implementation API signature
    :param func_obj:
    :param all_mtd_sig:
    :param source_project_dir:
    :param all_edge:
    :return:
    '''
    if func_obj.outer_class_name in service_impl_dict:
        service_impl = service_impl_dict[func_obj.outer_class_name]
    else:
        service_impl = search_service_impl(func_obj, all_mtd_sig, source_project_dir)
        if not service_impl:
            return ""
        service_impl_dict[func_obj.outer_class_name] = service_impl

    service_impl_sig = format_impl_class_mtd_sig(service_impl, func_obj.func_name, all_edge)
    return service_impl_sig


def main(binary_project_dir:str, source_project_dir:str):
    '''
    Main Process
    :param binary_project_dir:
    :param source_project_dir:
    :return:
    '''
    prepare_work(binary_project_dir, source_project_dir)

    all_mtd_sig = collect_all_mtd_sig(jar_dir, API_dir)
    all_edge = get_all_edges(call_graph_dir)
    gRPC_func_list = get_gRPC_func_list(proto_desc_dir)

    for func_obj in gRPC_func_list:
        service_impl_sig = format_service_impl_sig(func_obj, all_mtd_sig, source_project_dir, all_edge)
        if service_impl_sig:
            all_edge = update_cg_edge(service_impl_sig, func_obj, all_edge)
    write_call_graph(all_edge, "./unified_call_graph")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Generate unified call graph patched with gRPC invocation.",
                                     epilog="Example usage: python main.py --project-dir /path/to/project"
                                     )
    parser.add_argument('--bin_project_dir', type=str, required=True, help="The binary directory of the project, your project should be in complied format(i.e., .class files).")
    parser.add_argument('--src_project_dir', type=str, required=True, help="The source code directory of the project, your project should be in source code format(i.e., .java files).")

    args = parser.parse_args()

    main(args.bin_project_dir, args.src_project_dir)

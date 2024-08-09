"""
# File       :fuzzyMatch.py
# version    :python 3.9
# Description：
"""
from gRPCfunc import gRPCfunc
from utils.commonUtils import split_camel_word
from utils.JavaSrcUtils import interface_impl_class_search


def concatenate_adjacent_strings(str_list):
    '''
    Concate adj element in a list
    :param str_list:
    :return:
    '''
    concatenated_list = []
    for i in range(len(str_list) - 1):
        concatenated_list.append(str_list[i] + str_list[i + 1])
    return concatenated_list


def comp_suffix_match(interface_name:str, parent_path_name:str, clz_name:str, suffix_list:list):
    '''
    complete suffix match
    :param interface_name:
    :param parent_path_name:
    :param clz_name:
    :param suffix_list:
    :return:
    '''
    for suffix in suffix_list:
        ideal_name = interface_name + suffix
        if ideal_name == clz_name and parent_path_name != "proto":
            return True
    return False


def part_suffix_match(interface_name:str, clz_name:str, suffix_list:list):
    '''
    partial suffix match
    :param interface_name:
    :param clz_name:
    :param suffix_list:
    :return:
    '''
    interface_word_list = [i for i in split_camel_word(interface_name) if i !=""]
    merged_word_list = concatenate_adjacent_strings(interface_word_list)
    interface_word_list.extend(merged_word_list)
    for suffix in suffix_list:
        for word in interface_word_list:
            ideal_name = word + suffix
            if ideal_name == clz_name:
                return True
    return False


def fuzzy_match(gRPC_func:gRPCfunc, mtd_sig_list:list):
    '''
    Fuzzy match the impl method signature from the signature list
    :param gRPC_func:
    :param mtd_sig_list:
    :return:
    '''
    suffix_list = ["Handler", "Service", "Impl"]
    comp_match_lst = []
    part_match_lst = []

    clz_name_list = set([i.split(":")[0] for i in mtd_sig_list])

    for impl_clz_name in clz_name_list:
        full_clz_name = impl_clz_name
        if "." in impl_clz_name:
            impl_clz_name = impl_clz_name.split(".")[-1]
            parent_folder_name = full_clz_name.split(".")[-2]
        else:
            continue
        sub_clz_name = impl_clz_name

        if "$" in impl_clz_name:
            sub_clz_name = impl_clz_name.split("$")[-1]

        if comp_suffix_match(gRPC_func.outer_class_name, parent_folder_name, sub_clz_name, suffix_list):
            comp_match_lst.append(full_clz_name)
        elif part_suffix_match(gRPC_func.outer_class_name, sub_clz_name, suffix_list):
            part_match_lst.append(full_clz_name)

    if len(comp_match_lst):
        return comp_match_lst
    else:
        return part_match_lst


def search_service_impl(gRPC_func, mtd_sig_list:list, source_project_dir:str):
    '''
    Match the service impl class
    :param gRPC_func:
    :param mtd_sig_list:
    :param source_project_dir:
    :return:
    '''
    impl_clz_name_can = fuzzy_match(gRPC_func, mtd_sig_list)
    if not len(impl_clz_name_can):
        return ""
    impl_class_list = interface_impl_class_search(gRPC_func, impl_clz_name_can, source_project_dir)
    if not len(impl_class_list):
        return ""
    impl_clz_name = impl_class_list[0]
    return impl_clz_name
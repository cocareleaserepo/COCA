"""
# File       :JavaSrcUtils.py
# version    :python 3.9
# Description：
"""
import javalang
import os
import gRPCfunc


def check_implements_interface(node, interface_name:str)->bool:
    '''
    Check whether the code node implement the certain interface
    :param node:
    :param interface_name:
    :return:
    '''
    if node.implements is None:
        return False
    return any(interface.name == interface_name for interface in node.implements)


def locat_file(directory:str, parent_dir_name:str, clz:str)->str:
    '''
    Locate the java file for the class
    :param directory:
    :param parent_dir_name:
    :param clz:
    :return:
    '''
    file_name = clz + ".java"

    for root, dirs, files in os.walk(directory):
        if file_name in files:
            parent_dir = os.path.basename(os.path.dirname(os.path.join(root, file_name)))
            if parent_dir == parent_dir_name:
                return os.path.join(root, file_name)
    return None


def parse_java_code_clz_dict(java_code:str)->dict:
    '''
    Parse the java src file to obtain the class directory
    :param java_code:
    :return:
    '''
    tree = javalang.parse.parse(java_code)
    class_dict = {}

    for path, node in tree:
        if isinstance(node, javalang.tree.ClassDeclaration):
            class_name = node.name
            superclass = node.extends.name if node.extends else None
            if node.implements:
                interfaces = [interface.name for interface in node.implements]
            else:
                interfaces = []
            class_dict[class_name] = {
                'node': node,
                'superclass': superclass,
                'interfaces': interfaces
            }
    return class_dict


def is_sub_interface_impl(file_name:str, interface_name:str, sub_clz:str)->bool:
    '''
    Check whether the sub class implement the interface
    :param file_name:
    :param interface_name:
    :param sub_clz:
    :return:
    '''

    with open(file_name, "r") as f:
        java_code = f.read()

    class_dict = parse_java_code_clz_dict(java_code)
    subclass_node = class_dict[sub_clz]['node']
    if check_implements_interface(subclass_node, interface_name):
        return True
    return False


def is_interface_impl(file_name:str, interface_name:str, clz_name:str)->bool:
    '''
    Check whether the class implement the certain interface
    :param file_name:
    :param interface_name:
    :param clz_name:
    :return:
    '''
    with open(file_name, "r") as f:
        java_code = f.read()

    tree = javalang.parse.parse(java_code)
    for path, node in tree:
        if isinstance(node, javalang.tree.ClassDeclaration) and node.name == clz_name:
            return check_implements_interface(node, interface_name)
    return False


def interface_impl_class_search(gRPC_func:gRPCfunc.gRPCfunc, impl_clz_name_can:list, project_dir:str)->list:
    '''
    Find the impl class for the interface
    :param gRPC_func:
    :param impl_clz_name_can:
    :param project_dir:
    :return:
    '''
    candidate_clz_list = []
    clz_list = list(set([(clz.split(".")[-2], clz.split(".")[-1]) for clz in impl_clz_name_can if "." in clz]))
    interface_name = gRPC_func.outer_class_name
    for up_folder, clz in clz_list:
        if "$" in clz:
            super_clz, sub_clz = clz.split("$")[0], clz.split("$")[1]
            file_name = locat_file(project_dir, up_folder, super_clz)
            if not file_name:
                continue
            is_impl = is_sub_interface_impl(file_name, interface_name, sub_clz)
        else:
            file_name = locat_file(project_dir, up_folder, clz)
            if not file_name:
                continue
            is_impl = is_interface_impl(file_name, interface_name, clz)
        if is_impl:
            candidate_clz_list.append(clz)
    return candidate_clz_list
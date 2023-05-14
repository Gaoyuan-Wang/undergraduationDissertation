import ntpath
import os
import random

import numpy as np
import torch
from torch.backends import cudnn


from configs import decode_config
from data.single_dataset import SingleDataset
from models import create_model
from options.test_options import TestOptions
import torch
import torch.nn
import onnx

def set_seed(seed):
    cudnn.benchmark = False  # if benchmark=True, deterministic will be False
    cudnn.deterministic = True
    np.random.seed(seed)
    random.seed(seed)
    torch.manual_seed(seed)
    torch.cuda.manual_seed(seed)
    torch.cuda.manual_seed_all(seed)


if __name__ == '__main__':
    opt = TestOptions().parse()

    set_seed(opt.seed)
    if opt.config_str is not None:
        assert 'super' in opt.netG or 'sub' in opt.netG
        config = decode_config(opt.config_str)
    dataloader = torch.utils.data.DataLoader(
        SingleDataset(opt),
        batch_size=opt.batch_size,
        shuffle=not opt.serial_batches,
        num_workers=opt.num_threads)
    model = create_model(opt)
    model.setup(opt, verbose=False)
    device = torch.device('cuda')

    model = torch.load('/home/scu/gaoyuan/jupyter_file/compressed/latest_net_G.pth', map_location=device)
    model.eval()

    input_names = ['input']
    output_names = ['output']

    x = torch.randn(1, 3, 224, 224, device=device)

    torch.onnx.export(model, x, '/home/scu/gaoyuan/jupyter_file/conversion/latest_net_G.onnx', input_names=input_names, output_names=output_names, verbose='True')
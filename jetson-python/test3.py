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
from utils import html, util
import requests
from urllib3 import encode_multipart_formdata

def set_seed(seed):
    cudnn.benchmark = False  # if benchmark=True, deterministic will be False
    cudnn.deterministic = True
    np.random.seed(seed)
    random.seed(seed)
    torch.manual_seed(seed)
    torch.cuda.manual_seed(seed)
    torch.cuda.manual_seed_all(seed)


def save_images(path, visuals, image_path, opt):
    def convert_visuals_to_numpy(visuals):
        for key, t in visuals.items():
            tile = opt.batch_size > 8
            if key == 'labels':
                t = util.tensor2label(t, opt.input_nc + 2, tile=tile)
            else:
                t = util.tensor2im(t, tile=tile)
            visuals[key] = t
        return visuals

    visuals = convert_visuals_to_numpy(visuals)

    short_path = ntpath.basename(image_path[0])
    name = os.path.splitext(short_path)[0]

    for label, image_numpy in visuals.items():
        image_name = os.path.join(label, '%s.png' % (name))
        save_path = os.path.join(path, image_name)
        util.save_image(image_numpy, save_path, create_dir=True)


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
    for i, data in enumerate(dataloader):
        model.set_input(data)  # unpack data from data loader
        if i == 0 and opt.need_profile:
            model.profile(config)
        model.test(config)  # run inference
        visuals = model.get_current_visuals()  # get image results
        save_images(opt.results_dir, visuals, model.get_image_paths(), opt)
    del model
    torch.cuda.empty_cache()
    file_data = {'file': ('reality.jpg', open('/home/gaoyuan/GAN/result/fake_B/reality.png', 'rb').read())}
    encode_data = encode_multipart_formdata(file_data)
    data = encode_data[0]
    header = {'Content-Type': encode_data[1]}
    requests.post('https://gaoyuanwang.top/cartoonUpload', headers=header, data=data)
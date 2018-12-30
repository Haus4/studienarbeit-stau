
class ImageProcessor:
    def __init__(self, camera):
        self.camera = camera

    def process(self, image):
        info = {}
        info["jam"] = False
        info["vehicles"] = 0

        return [info, info]

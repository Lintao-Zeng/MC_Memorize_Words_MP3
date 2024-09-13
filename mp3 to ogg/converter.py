import os

for i in range(1, 26):
    os.system('ffmpeg -i input/' + str(i) + '.mp3 output/' + str(i) + '.ogg')

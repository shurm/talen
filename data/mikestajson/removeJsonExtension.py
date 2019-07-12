#!/usr/bin/python

import os

# Usage:
# removes the .json recursively

def convert(currentPath):
    for fname in os.listdir(currentPath):
        newPath = currentPath+'/'+fname
        if(os.path.isfile(newPath)):
            filename, file_extension = os.path.splitext(fname)
            if(file_extension=='.json'):
                renamedPath = currentPath+'/'+filename
                os.rename(newPath, renamedPath)
        else:
            convert(newPath)

convert('.')

#!/usr/bin/env python3

import os
import re
import sys

if __name__ == '__main__':
    def f2s(p) -> str:
        with open(p, 'r') as f:
            return ''.join(f.readlines())

    current_dir: str = os.path.abspath(os.path.dirname(sys.argv[0]))
    project_root = os.path.join(current_dir, '..')
    version: str = f2s(os.path.join(project_root, 'version.txt')).strip()
    assert version is not None, 'the version should not be empty'
    readme: str = f2s(os.path.join(current_dir, 'README.md'))
    readme = re.sub('__VERSION__', version, readme)
    root_readme = os.path.join(project_root, 'README.md')
    with open(root_readme, 'w') as root_readme_file:
        root_readme_file.write(readme)

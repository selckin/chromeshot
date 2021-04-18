# chromeshot

[![Main build](https://github.com/selckin/chromeshot/actions/workflows/build.yml/badge.svg)](https://github.com/selckin/chromeshot/actions/workflows/build.yml)

1) Download [chromeshot-*-simple.zip](https://github.com/selckin/chromeshot/releases/latest/) and unpack

   To see all the options:
   ```bash
    ./chromeshot/bin/chromeshot --help
    ```
   
2) Run google chrome with remote debugging enabled

    ```bash
    google-chrome --remote-debugging-port=9222
    ```

   Make sure it is not already running, else it will just open a new window and not apply the command line arguments.

   If you run it from a console it will output something like

   ```DevTools listening on ws://127.0.0.1:9222/devtools/browser/ad475ba1-233d-40cc-8865-befb84faaf3b```

   You can also visit <chrome://inspect/#devices> and see it listed under "Remote Target"


3) Open the website the html you want to screenshot for example <https://www.typescriptlang.org/tsconfig>
  
4) Run chromeshot
   ```bash
    ./chromeshot/bin/chromeshot -t ~/tmp/chromeshots/ --tab-url www.typescriptlang.org --grab-selector .compiler-option --file-selector h3 --file-attribute id


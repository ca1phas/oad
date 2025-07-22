## Getting Started

1. Install GitHub Desktop: https://desktop.github.com/download/
2. Install Visual Studio Code: https://code.visualstudio.com/docs/setup/windows
3. To setup Java in Visual Studio Code, follow the tutorial here: https://code.visualstudio.com/docs/languages/java

## Before You Start Coding

1. Create a new branch through GitHub Desktop named after the module that you are working on.
2. `Publish branch`.

## After You Done Coding

1. Upload the changes by entering the summary and commit to the `branch`.
2. `Push origin`.
3. [If complete all coding in the module] `Preview Pull Request` + `Create Pull Request`

## DON'Ts

1. **DO NOT** upload any changes to the `master` branch
2. **DO NOT** merge the `master` branch with any other branch

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

In `src` folder:

- `util`: Contains helper classes for common functionality like file handling, ID generation, and pagination.
- `model`: Defines core data models (e.g., User, Book, Reservation) and enums used throughout the system.
- `repository`: Handles low-level CRUD operations and file I/O for each model using text files.
- `service`: Implements business logic and orchestrates interactions between models, repositories, and utilities.
- `controller`: Manages input/output flow between the user interface and the service layer. Exception handling should also be done here.
- `view`: Provides console-based UI for displaying information and capturing user interactions.

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.

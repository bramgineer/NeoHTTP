# NeoHTTP

---

# Neovim + Maven Integration for Java Development

![Neovim](https://img.shields.io/badge/Neovim-v0.5+-brightgreen)
![Maven](https://img.shields.io/badge/Maven-3.6.3-blue)
![Java](https://img.shields.io/badge/Java-11-orange)

Welcome to the **Neovim + Maven Integration** project! This guide will help you set up a productive and automated development environment for Java using Neovim and Maven. Whether you're a seasoned Java developer or just getting started, this setup will streamline your workflow and boost your productivity.

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
  - [1. Install Necessary Tools](#1-install-necessary-tools)
  - [2. Configure Neovim](#2-configure-neovim)
- [Usage](#usage)
  - [Custom Maven Commands](#custom-maven-commands)
  - [Telescope for Navigation](#telescope-for-navigation)
  - [Automated Build and Run](#automated-build-and-run)
- [Contributing](#contributing)
- [License](#license)

## Features

- **Integrated Maven Commands**: Clean, build, and run your Maven projects directly from Neovim.
- **LSP Support**: Leverage the power of Language Server Protocol for enhanced Java development.
- **Telescope Integration**: Easily navigate your project files and search code with fuzzy finding.
- **Automated Workflows**: Automatically build and run your projects on file save.

## Prerequisites

- Neovim (v0.5 or later)
- Java Development Kit (JDK)
- Apache Maven
- Plugin Manager (e.g., `packer.nvim`)

## Installation

### 1. Install Necessary Tools

#### Neovim

Download and install the latest version of Neovim from [here](https://neovim.io/).

#### Maven

Install Maven according to your operating system:

- **Windows**: [Download and setup guide](https://maven.apache.org/install.html)
- **macOS**: 
  ```sh
  brew install maven
  ```
- **Linux**:
  ```sh
  sudo apt-get install maven
  ```

#### Plugin Manager

Install `packer.nvim` by adding the following to your `init.lua`:

```lua
-- Bootstrap packer.nvim if it is not installed
local install_path = vim.fn.stdpath('data')..'/site/pack/packer/start/packer.nvim'
if vim.fn.empty(vim.fn.glob(install_path)) > 0 then
  vim.fn.system({'git', 'clone', '--depth', '1', 'https://github.com/wbthomason/packer.nvim', install_path})
  vim.cmd 'packadd packer.nvim'
end
```

### 2. Configure Neovim

Add the following configuration to your `init.lua`:

```lua
-- init.lua

require('packer').startup(function()
  use 'wbthomason/packer.nvim' -- Plugin manager
  use 'neovim/nvim-lspconfig' -- LSP configurations
  use 'nvim-telescope/telescope.nvim' -- Fuzzy finder
  use 'nvim-lua/plenary.nvim' -- Dependency for telescope
end)

-- LSP settings
local nvim_lsp = require('lspconfig')

-- Java LSP configuration
nvim_lsp.jdtls.setup{
  cmd = {'path/to/jdtls'}, -- Replace with the path to your jdtls executable
  root_dir = nvim_lsp.util.root_pattern('.git', 'pom.xml')
}

-- Define custom commands for Maven
vim.cmd([[
  command! MavenClean :!mvn clean
  command! MavenInstall :!mvn install
  command! MavenRun :!mvn exec:java -Dexec.mainClass="com.example.App"
]])

-- Keybindings for custom Maven commands
vim.api.nvim_set_keymap('n', '<leader>mc', ':MavenClean<CR>', { noremap = true, silent = true })
vim.api.nvim_set_keymap('n', '<leader>mi', ':MavenInstall<CR>', { noremap = true, silent = true })
vim.api.nvim_set_keymap('n', '<leader>mr', ':MavenRun<CR>', { noremap = true, silent = true })

-- Telescope setup and keybindings
require('telescope').setup{
  defaults = {
    file_ignore_patterns = {"node_modules", ".git"}
  }
}

vim.api.nvim_set_keymap('n', '<leader>ff', ':Telescope find_files<CR>', { noremap = true, silent = true })
vim.api.nvim_set_keymap('n', '<leader>fg', ':Telescope live_grep<CR>', { noremap = true, silent = true })
vim.api.nvim_set_keymap('n', '<leader>fb', ':Telescope buffers<CR>', { noremap = true, silent = true })
vim.api.nvim_set_keymap('n', '<leader>fh', ':Telescope help_tags<CR>', { noremap = true, silent = true })

-- Run Maven install and exec on saving Java files
vim.cmd([[
  autocmd BufWritePost *.java silent! :!mvn install
  autocmd BufWritePost *.java silent! :!mvn exec:java -Dexec.mainClass="com.example.App"
]])
```

## Usage

### Custom Maven Commands

- **Clean Project**: `:MavenClean` or `<leader>mc`
- **Install Project**: `:MavenInstall` or `<leader>mi`
- **Run Project**: `:MavenRun` or `<leader>mr`

### Telescope for Navigation

- **Find Files**: `<leader>ff`
- **Live Grep**: `<leader>fg`
- **Buffers**: `<leader>fb`
- **Help Tags**: `<leader>fh`

### Automated Build and Run

Automatically build and run your project on saving Java files with the autocommands:

```sh
autocmd BufWritePost *.java silent! :!mvn install
autocmd BufWritePost *.java silent! :!mvn exec:java -Dexec.mainClass="com.example.App"
```

## Contributing

We welcome contributions! Please fork the repository and submit a pull request for any improvements or bug fixes.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

Feel free to reach out if you have any questions or need further assistance. Happy coding!

---

This README aims to provide clear instructions and highlight the benefits of using Neovim with Maven for Java development.
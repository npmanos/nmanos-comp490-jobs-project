# Project 1 - Jobs API

[![build](https://github.com/npmanos/nmanos-comp490-jobs-project/actions/workflows/gradle.yml/badge.svg)](https://github.com/npmanos/nmanos-comp490-jobs-project/actions/workflows/gradle.yml) [![lint](https://github.com/npmanos/nmanos-comp490-jobs-project/actions/workflows/super-linter.yml/badge.svg)](https://github.com/npmanos/nmanos-comp490-jobs-project/actions/workflows/super-linter.yml)

Author: Nick Manos

A small utility to save 50 Google Jobs Search results to a text file.

## Installation

### Requirements

- [JDK 21](https://adoptium.net/marketplace/?version=21)
- [SerpApi API Key](https://serpapi.com/)

### Build Instructions

1. Clone the repo and open the project folder

   ```bash
   git clone https://github.com/npmanos/nmanos-comp490-jobs-project.git
   cd nmanos-comp490-jobs-project
   ```

2. Create the environment file

   ```bash
   cp sample.env .env
   ```

3. Add your SerpApi API key to the .env file

   ```text
   JOBSPROJ_API_KEY=your_key_here
   ```

4. Build the project

   ```bash
   ./gradlew installDist
   ```

## Usage

You can run the application by calling `dist/bin/job-search`

```text
dist/bin/job-search --help

Usage: job-search [<options>]

  This application saves 50 results from a Google job search for <query> to
  <output>.

  You can customize <query> and <output> using the options below.

Options:
  -q, --query=<text>   Job search query (default: software engineer boston)
  -o, --output=<path>  Output file location (default: output/jobs.txt)
  -h, --help           Show this message and exit
```

## TODO

- [ ] Write more unit tests
- [ ] Write KDoc comments
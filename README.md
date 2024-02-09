# Project 1 - Jobs API

[![build](https://github.com/npmanos/nmanos-comp490-jobs-project/actions/workflows/gradle.yml/badge.svg)](https://github.com/npmanos/nmanos-comp490-jobs-project/actions/workflows/gradle.yml) [![lint](https://github.com/npmanos/nmanos-comp490-jobs-project/actions/workflows/super-linter.yml/badge.svg)](https://github.com/npmanos/nmanos-comp490-jobs-project/actions/workflows/super-linter.yml)

Author: Nick Manos

A small utility to store 50 Google Jobs Search results in a database and write all stored jobs to a text file.

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

After building the project, run `dist/bin/job-search` in your terminal.

> [!IMPORTANT]
> There is a known bug which can cause file writing to take a long time. If the application seems to be frozen on `Saving file...` please be patient.
> This may be resolved in v2.0.0+ but may still occur in certain circumstances.

```text
dist/bin/job-search --help

Usage: job-search [<options>]

  This application saves 50 results from a Google job search for <query> to <database> and writes all job results to <output>.

  NOTE: Saving to <output> may take a few minutes. If the application seems frozen, please be patient.

  You can customize <query>, <database>, and <output> using the options below.

Options:
  -q, --query=<text>     Job search query (default: software engineer boston)
  -d, --database=<text>  Database file location (default: output/jobs.db)
  -o, --output=<path>    Output file location (default: output/jobs.txt)
  -h, --help             Show this message and exit

```

## Database Structure

Each object in the `jobs_results` array of the API response is stored in the JobDAO table with `jobId` as the primary key,
except for the `job_highlights`, `related_links`, `extensions`, and `detected_extensions` arrays, which are stored in
their own tables using `jobId` as a foreign key in order to implement a many-to-one relationship. The QueryDAO table
tracks what searches returned a particular result also using a `jobId` foreign key many-to-one relationship.

![Project database structure](img/job_db_diagram.png)

## TODO

- [ ] Prove slow file writing has been fully resolved
  - [ ] Write a unit test to catch regressions
- [ ] Write KDoc comments
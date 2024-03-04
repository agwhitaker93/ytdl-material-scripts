# ytdl-material-scripts

A collection of useful scripts to make ytdl-material a bit more manageable

## Common env vars

- api_url - The API URL for your ytdl-material instance
- api_key - The API key for your ytdl-material instance

## get_subs.clj

Saves all your current ytdl subs to a file named `subs-list.edn`

## restart_downloads.clj

Monitors ytdl-material's download queue, and ensures up to `dl-limit` number of downloads are in progress.
Exits when there are no more unfinished downloads.

### env vars

- dl-limit - the maximum number of downloads to have going at a time
- wait-seconds - how long to wait between polls

## subs_vid_count

Checks how many videos a subscription should have, and compares it to how many ytdl-material has.

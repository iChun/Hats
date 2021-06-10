Hats Mod Source Repo
====================

## Building the Mod

Publish iChunUtil to local Maven repo. Rip out it's `accesstransformer.cfg` and put it in `/src/api/resource/META-INF/` (follow the file path in `build.gradle`)


## Making a Hat

You will need to use Tabula. Hats adds a new button in the Toolbar for Hats, you can Make a New Hat (Hold SHIFT when in the Open Hat window) or browse hats from the hats folder there. You will see a ghost template of a Steve head. Build your hat around that.

When you are done, take the save file from `/mods/tabula/saves/` and move it to `/mods/hats/`. You can restart the game or reload the hats in-game to load it up.

Hats also supports additional metadata (In Tabula: Edit Project -> Edit Metadata):

| Meta Tag                              | Description |
| --------------------------------- | ----------- |
| `hats-rarity`                     |  Forces a rarity onto the Hat. Allows `common`, `uncommon`, `rare`, `epic`, `legendary`.           |
| `hats-pool`                       |  Forces the Hat to use a specific Hat Pool. Use the same pool as other Hats to share a pool when randomly spawning.           |
| `hats-worth`                      |  Forces a Hat to have a specific HAT value.           |
| `hats-contributor-uuid`           |  Legacy code: UUID for past contributors to the Hats mod. If your UUID matches one of the Hats, you get one for free.           |
| `hats-contributor-mini-me`        |  Legacy code: Other than a tag, this is not used.           |
| `hats-accessory`                  |  Defines that this Hat is an accessory of another Hat file. Use file names (without extension).           |
| `hats-accessory-layer`            |  Conflict layers. Two accessories conflict if they have the same layer. Supports multiple layers.           |
| `hats-accessory-parent`           |  Defines that this Hat is an accessory of another accessory of a Hat. Use file nemes (without extension) as well.           |
| `hats-accessory-hide-parent-part` |  Hides a parent's (either the accessory or the base Hat) part. See Pig and SuperPig for example. Supports multiples.           |
| `hats-description`                |  Hat description to put in the Hat Tooltip.           |

Separate the Meta Tag and the value with a `:`. Example: `hats-rarity:uncommon`, or `hats-description:Just a simple Farmer's Hat`.


## Supporting Mod Mobs

You will also need Tabula. If you are able to, open the mod mob model from the `Import From Minecraft` button in the Toolbar. Some mods may not be supported and will not show up, or will be a blank project.

Click on the Head model piece and click on the `Export Project` button and click `Head Info JSON`. Adjust the values to get the Googly Eyes and the Half Slab to appear properly. Generally the Googly Eyes are meant to cover/overlap the eyes, and the half slab should cover the widest part of the head, from the center.

To disable Hats or Googly Eyes support, `Disable Hat Info` or `Disable Eye Info`.

Once exported, you can find the JSON file in `/mods/tabula/export/`.

This text file is to explain how HatModMobSupport.json works.

The JSON file is structured in a way where { <className> { <property> : <propertyType>...etc } }

Examples of classNames:
net.minecraft.entity.passive.EntityCow
tropicraft.entities.passive.water.EntityMarlin
biomesoplenty.entities.EntityJungleSpider

List of accepted properties:
canUnlockHat (this accepts only true/false, the rest accepts the listed property types below)
prevRenderYawOffset
renderYawOffset
prevRotationYawHead
rotationYawHead
prevRotationPitch
rotationPitch
rotatePointVert
rotatePointHori
rotatePointSide
offsetPointVert
offsetPointHori
offsetPointSide
hatScale

List of accepted property types:
prevRenderYawOffset
renderYawOffset
prevRotationYawHead
rotationYawHead
prevRotationPitch
rotationPitch
Floats(Numbers, including decimal placing), in number of blocks. 1 block is normally 16 pixels (depends on the mob)

What is:
"Can Unlock Hat"           : When this mob is killed, will it unlock a hat?
"Render Yaw Offset"        : The sideways rotation of the "body" of the mob. Separate rotation from the head. If not set, will follow defaults of renderYawOffset.
"Rotation Yaw Head"        : The sideways rotation of the "head" of the mob. If not set, will follow defaults of rotationYawHead.
"Rotation Pitch"           : The up and down rotation of the "head". If not set, will follow defaults of rotationPitch.
"Rotate Point Vert(ical)"  : The rotation point of the head from the very bottom of the mob's bounding box. An 8x8x8 (basically half a block) head is assumed to be directly above this point unless an offset is set
"Rotate Point Hori(zontal)": The rotation point of the head forwards from the middle of the mob's bounding box.
"Rotate Point Side"        : The rotation point of the head to the side (for mobs which heads aren't centered. Think the Wither's smaller heads that's off to the side)
"Offset Point Vert(ical)"  : The offset (upwards and downwards) of the 8x8x8 head mentioned in "Rotate Point Vert(ical)"
"Offset Point Hori(zontal)": The offset (forwards and backwards) of the 8x8x8 head.
"Offset Point Side"        : The offset (left and right) of the 8x8x8 head.
"Hat Scale"                : The scale of the hat rendered on the mob. Defaults to no scaling (1.0) if not set.

How to create a hat mod mob mapping. (Disclaimer, my tutorial will be a bit confusing)
1. Find out the class (including the package) name of the mob you want to map. EG: "tropicraft.entities.passive.water.EntityMarlin"
2. Does the mob have a body and a head with different rotations? If yes, renderYawOffset is the same, you can put ignore putting a value in. EG: pigs, cows. If not, renderYawOffset (and prev) need to be set. Put in a value, like for ghasts: "renderYawOffset": 0.0. EG: ghasts (just a floating head). 
3. Can the mob's head rotate? If yes, omit rotationYawHead (and prev) so it'll follow defaults. If no, put a value in like for the above (step 2). Do the same for pitch.
4. At this point you might want to load up the game (save the JSON file to your /hats/ folder and enable the local mod mob mapping config, and set randomHat to 100). Look ingame and a hat should have spawned at the foot of the mob.
5. Once you confirmed this, you want to get the hat to the right positions. Try to figure out the rotation point of the head (reminder, on normal models/mobs, 1 pixel is 1/16th of a block) and set the appropriate values. Use the Hats GUI and hit Reload Hats to make it read the JSON file again.
6. Once you think you got those right, you want to set the offset values as well.
7. ???
8. Profit!
Additional note: This method of mob support only works for simple mobs. For more complicated mobs (multiple heads, strange rotations, etc), you'll have to use the mod's API which involves making a new mod. That, is obviously a lot more complicated.

Here we have an example (the cow's values are actually taken from the actual mod. Tweak around with those values to see how it works. Marlin's are faked):
{
  "net.minecraft.entity.passive.EntityCow": {
    "offsetPointVert": 1.25,
    "rotatePointHori": 0.5,
    "rotatePointVert": 1.2625,
    "offsetPointHori": 0.125
  },
  "tropicraft.entities.passive.water.EntityMarlin": {
    "canUnlockHat": false,
    "prevRotationYawHead": "prevRenderYawOffset",
    "rotationYawHead": "renderYawOffset",
    "prevRotationPitch": 0.0,
    "rotationPitch": 0.0
  }
}

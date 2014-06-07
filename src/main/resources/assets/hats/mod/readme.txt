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

Here we have an example:
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

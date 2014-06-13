package hats.common.techne;

import com.google.gson.annotations.SerializedName;

/**
 * Deserialized version of Techne 2's JSON save files.
 */
public class TC2Info
{
    public Techne Techne = new Techne();

    private class Techne
    {
        @SerializedName("@Version")
        String Version = "2.2";
        String Author = "NotZeuX";
        String Name = "";
        String PreviewImage = "";
        String ProjectName = "";
        String ProjectType = "";
        String Description = "";
        String DateCreated = "";
        Model[] Models = new Model[] { new Model() };

        private class Model
        {
            ModelInfo Model = new ModelInfo();

            private class ModelInfo
            {
                String GlScale = "1,1,1";
                String Name = "";
                String TextureSize = "64,32";
                @SerializedName("@texture")
                String texture = "texture.png";
                String BaseClass = "ModelBase";
                Group Geometry = new Group();
            }
        }
    }

    public class Group
    {
        Circular[] Circular = new Circular[] {};
        Shape[] Shape = new Shape[] {};
        Linear[] Linear = new Linear[] {};
        Null[] Null = new Null[] {};
    }

    public class Circular
    {
        @SerializedName("@Type")
        String Type = "16932820-ef7c-4b4b-bf05-b72063b3d23c";
        @SerializedName("@Name")
        String Name = "Circular Array";
        String Position = "0,0,0";
        String Rotation = "0,0,0";
        Group Children = new Group();
        int Count = 5;
        int Radius = 16;
    }

    public class Shape
    {
        int Id = 1; //is a variable
        @SerializedName("@Type")
        String Type = "d9e621f7-957f-4b77-b1ae-20dcd0da7751";
        @SerializedName("@Name")
        String Name = "new cube";
        String IsDecorative = "False";
        String IsFixed = "False";
        String IsMirrored = "False";
        String Position = "0,0,0";
        String Rotation = "0,0,0"; //TODO is in radians. Be sure to convert accordingly
        String Size = "1,1,1";
        String TextureOffset = "0,0";
    }

    public class Linear
    {
        @SerializedName("@Type")
        String Type = "fc4f63c9-8296-4c97-abd8-414f20e49bd5";
        @SerializedName("@Name")
        String Name = "Linear Array";
        String Position = "0,0,0";
        String Rotation = "0,0,0";
        Group Children = new Group();
        String Count = "0,0,0";
        String Spacing = "0,0,0";
    }

    public class Null
    {
        @SerializedName("@Type")
        String Type = "3b3bb6e5-2f8b-4bbd-8dbb-478b67762fd0";
        @SerializedName("@Name")
        String Name = "null element";
        String Position = "0,0,0";
        String Rotation = "0,0,0";
        Group Children = new Group();
    }
}

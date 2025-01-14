package me.x150.renderer.objfile;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.x150.renderer.util.RendererUtils;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Stack;

/**
 * A .mtl file
 */
public class MtlFile {
    final Stack<Material> materialStack = new Stack<>();
    ObjReader r;
    boolean inited = false;

    /**
     * Creates a new mtl file parser from the Reader
     *
     * @param inp The reader to read from
     *
     * @throws IOException When something goes wrong
     */
    public MtlFile(Reader inp) throws IOException {
        this.r = new ObjReader(inp);
    }

    /**
     * Reads and parses this mtl file
     *
     * @throws IOException When something goes wrong
     */
    public void read() throws IOException {
        if (inited) {
            throw new IllegalStateException("Already read");
        }
        this.inited = true;
        while (r.peek() != -1) {
            String s = r.readStr();
            switch (s) {
                case "newmtl" -> { // new material
                    Material item = new Material(r.readStr(), 1f, 1f, 1f, 1f, null);
                    materialStack.push(item);
                }
                case "Kd" -> {
                    Material peek = materialStack.peek();
                    peek.diffuseR = r.readFloat();
                    peek.diffuseG = r.readFloat();
                    peek.diffuseB = r.readFloat();
                }
                case "d", "Tr" -> {
                    Material peek = materialStack.peek();
                    float v = r.readFloat();
                    if (s.equals("Tr")) {
                        v = 1 - v;
                    }
                    peek.dissolve = v;
                }
                case "map_Kd" -> {
                    BufferedImage read = ImageIO.read(new File(r.readStr()));
                    Identifier tex = new Identifier("renderer", "dyntex-" + String.valueOf(Math.random()).hashCode());
                    materialStack.peek().diffuseTextureMap = tex;
                    RendererUtils.registerBufferedImageTexture(tex, read);
                }
            }
            r.skipLine(); // skip remaining
        }
    }

    /**
     * A simple material
     */
    @Data
    @AllArgsConstructor
    public static class Material {
        /**
         * The name of the material
         */
        String name;
        /**
         * Diffuse red
         */
        float diffuseR, /**
         * Diffuse green
         */
        diffuseG, /**
         * Diffuse blue
         */
        diffuseB;
        //        float diffuseR, diffuseG, diffuseB;
        /**
         * "Dissolve", aka the alpha
         */
        float dissolve;
        /**
         * The texture of this material, or null
         */
        Identifier diffuseTextureMap;
    }
}

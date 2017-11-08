package bilkent.cs565.paper.gl;

import glm.vec._3.Vec3;

public class GLM
{
    public static float dot(Vec3 a, Vec3 b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }
}

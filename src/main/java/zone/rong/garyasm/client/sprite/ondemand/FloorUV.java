package zone.rong.garyasm.client.sprite.ondemand;

public class FloorUV {

    public static FloorUV of(float u, float v) {
        return new FloorUV(u, v);
    }

    public final float u, v;

    private FloorUV(float u, float v) {
        this.u = u;
        this.v = v;
    }

    @Override
    public int hashCode() {
        long bits = 1L;
        bits = 31L * bits + (long) Float.floatToIntBits(u);
        bits = 31L * bits + (long) Float.floatToIntBits(v);
        return (int) (bits ^ (bits >> 32));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FloorUV)) {
            return false;
        }
        FloorUV uv = (FloorUV) o;
        return this.u == uv.u && this.v == uv.v;
    }

}

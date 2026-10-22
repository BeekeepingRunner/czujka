package pl.czujka.event;

/**
 * A rectangular map area in WGS 84 coordinates (the visible part of the map).
 */
public record BoundingBox(double minLon, double minLat, double maxLon, double maxLat) {

    public static final BoundingBox POLAND = new BoundingBox(14.07, 49.0, 24.15, 54.84);

    public BoundingBox {
        // NaN must be rejected explicitly: every comparison with NaN is false, so it would pass the checks below
        if (!Double.isFinite(minLon) || !Double.isFinite(minLat)
                || !Double.isFinite(maxLon) || !Double.isFinite(maxLat)) {
            throw new IllegalArgumentException("bbox must contain finite numbers");
        }
        if (minLon >= maxLon || minLat >= maxLat) {
            throw new IllegalArgumentException("bbox min values must be lower than max values");
        }
        if (minLon < -180 || maxLon > 180 || minLat < -90 || maxLat > 90) {
            throw new IllegalArgumentException("bbox is outside valid WGS 84 coordinates");
        }
    }

    /**
     * Parses {@code "minLon,minLat,maxLon,maxLat"}, the order used by MapLibre's {@code getBounds().toArray().flat()}.
     */
    public static BoundingBox parse(String value) {
        // limit -1 keeps trailing empty parts, so "1,2,3,4," is rejected instead of silently accepted
        String[] parts = value.split(",", -1);
        if (parts.length != 4) {
            throw new IllegalArgumentException("bbox must have the form minLon,minLat,maxLon,maxLat");
        }
        try {
            return new BoundingBox(
                    Double.parseDouble(parts[0].strip()),
                    Double.parseDouble(parts[1].strip()),
                    Double.parseDouble(parts[2].strip()),
                    Double.parseDouble(parts[3].strip()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("bbox must contain four numbers", e);
        }
    }
}

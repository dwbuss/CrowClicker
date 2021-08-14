package com.example.clicker;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;


public class ExpandedMBTilesTileProvider implements TileProvider {
    private final File source;
    private final int width;
    private final int height;


    public ExpandedMBTilesTileProvider(final File source, final int width, final int height) {
        if (source == null) {
            throw new IllegalArgumentException("source cannot be null");
        }
        this.source = source;
        this.width = width;
        this.height = height;
    }

    @Override
    public Tile getTile(final int x, int y, final int zoom) {
        Tile tile = NO_TILE;

        y = tms2gmaps(y, zoom);
        byte[] bitmap = retrieveBitmap(x, y, zoom);
        if (bitmap != null) {
            tile = new Tile(width, height, bitmap);
        }

        return tile;
    }

    private byte[] retrieveBitmap(final int x, final int y, final int zoom) {
        byte[] bitmap = null;

        if (zoom > 0) {
            bitmap = readBitmapFromDB(x, y, zoom);
            if (bitmap == null) {
                bitmap = crop(retrieveBitmap(x / 2, y / 2, zoom - 1), x, y);
            }
        }

        return bitmap;
    }

    private byte[] readBitmapFromDB(final int x, final int y, final int zoom) {
        byte[] bitmap = null;

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = SQLiteDatabase.openDatabase(source.getPath(), null, SQLiteDatabase.OPEN_READONLY);

            cursor = db.query("tiles",
                    new String[]{"tile_data"},
                    "tile_column=? and tile_row=? and zoom_level=?",
                    new String[]{"" + x, "" + y, "" + zoom},
                    null,
                    null,
                    null);

            if (cursor != null && cursor.moveToFirst()) {
                bitmap = cursor.getBlob(0);
            }
        } catch (final Exception e) {
            Log.e("Error reading database", e.getMessage());
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
            }

            try {
                if (db != null) {
                    db.close();
                }
            } catch (Exception e) {
            }
        }

        return bitmap;
    }

    private byte[] crop(final byte[] bmp, final int x, final int y) {
        byte[] output = bmp;
        if (bmp == null) {
            return output;
        }

        Bitmap bitmap = BitmapFactory.decodeByteArray(bmp, 0, bmp.length);
        bitmap = Bitmap.createBitmap(
                bitmap,
                x % 2 == 0 ? 0 : bitmap.getWidth() / 2,
                y % 2 == 0 ? bitmap.getHeight() / 2 : 0,
                bitmap.getWidth() / 2,
                bitmap.getHeight() / 2
        );

        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        bitmap.recycle();
        output = stream.toByteArray();

        return output;
    }

    private int tms2gmaps(final int y, final int zoom) {
        final int ymax = 1 << zoom;
        return ymax - y - 1;
    }
}
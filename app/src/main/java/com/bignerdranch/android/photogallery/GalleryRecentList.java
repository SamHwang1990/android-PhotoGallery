package com.bignerdranch.android.photogallery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by sam on 16/8/17.
 */
public class GalleryRecentList {
    class Photos {
        private List<GalleryItem> photo = new ArrayList<>();

        public List<GalleryItem> getPhoto() {
            return photo;
        }

        public void setPhoto(List<GalleryItem> photo) {
            this.photo = photo;
        }


    }

    public Photos photos;
    public String stat;

    public Photos getPhotos() {
        return photos;
    }

    public void setPhotos(Photos photos) {
        this.photos = photos;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }
}

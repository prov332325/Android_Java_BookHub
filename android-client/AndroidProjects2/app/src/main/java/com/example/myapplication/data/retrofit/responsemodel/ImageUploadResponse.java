package com.example.myapplication.data.retrofit.responsemodel;

import android.net.Uri;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ImageUploadResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("image_uri")
    private Uri imageUri;


    @SerializedName("filePaths")
    private List<String> filePaths; // 여기서 List<String> 으로 정의해야 함
    // 또는 단일 URL을 처리할 수도 있지만, 여기서는 여러 개의 URL을 예상

    // getter 메소드 필요
    public List<String> getFilePaths() { return filePaths; }



// 이미지 이름
    @SerializedName("fileName")
    private List<String> fileName; // 여기서 List<String> 으로 정의해야 함/

    public List<String> getFileName() {
        return fileName;
    }

    // uri
    @SerializedName("imgUris")
    private List<Uri> imgUris; // 여기서 List<String> 으로 정의해야 함
    // 또는 단일 URL을 처리할 수도 있지만, 여기서는 여러 개의 URL을 예상

    // uri getter
    public List<Uri> getImgUris() {
        return imgUris;
    }

    // 이미지 테이블에 있는 내용 추가함
    @SerializedName("board_image_number")
    private int board_image_number;
    @SerializedName("board_number")
    private int board_number;

    public Uri getImageUri() {
        return imageUri;
    }

    // ImageUploadResponse 생성자 1 - 서버에서 사진 가져올때 쓰기 !!
    public ImageUploadResponse(String imageUrl, Uri imageUri, int board_image_number, int board_number) {
        this.imageUrl = imageUrl;
        this.imageUri = imageUri;
        this.board_image_number = board_image_number;
        this.board_number = board_number;
    }

    // ImageUploadResponse 생성자 2 - 갤러리에서 사진 가져올때 쓰기 !!
    public ImageUploadResponse(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getBoard_image_number() {
        return board_image_number;
    }

    public int getBoard_number() {
        return board_number;
    }



    // 이미지를 목록으로 가져올때 사용할 내부 클래스
    public class ImageUploadResponse2 {
        @SerializedName("code")
        private int code;

        @SerializedName("message")
        private String message;

        @SerializedName("item")
        private List<ImageUploadResponse> images; // 이미지 여러장 목록


        // getter
        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public List<ImageUploadResponse> getImages() {
            return images;
        }
    }

}

package com.levent.pcd.service;

import java.io.File;
import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;

public interface AWSS3Helper {
	
	
	String deleteFileByKey(String fileName);
	String putObject(MultipartFile file, String fileName)
			throws AmazonServiceException, SdkClientException, IOException;
	String getStreamFromUrl(String filename) throws IOException;
	File getFileFromUrl(String filename) throws IOException;
	String getFileUrlByName(String filename);
	
}

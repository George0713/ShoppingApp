package com.levent.pcd.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levent.pcd.config.AWSConfig;
import com.levent.pcd.model.Product;
import com.levent.pcd.repository.ProductRepository;

@Component

public class UploadImageToS3Once {

	@Autowired
	AWSLogin helper;
	@Autowired
	ProductRepository rep;

	@Autowired
	AWSConfig awsConfig;

	public void doUpload() throws AmazonServiceException, SdkClientException, URISyntaxException, IOException {
		AmazonS3 client = helper.getAmazonS3Client();
		String fileName = "data/products.json";
		File file = new File(getClass().getClassLoader().getResource(fileName).getFile());
		File file_done = new File(getClass().getClassLoader().getResource("products_done.json").getFile());
		if(!file_done.exists()) {
			file_done.createNewFile();
		}
		ObjectMapper mapper = new ObjectMapper();
		Product[] products = mapper.readValue(file, Product[].class);
		PrintWriter writer = new PrintWriter(file_done);
		for (Product product : products) {
			File file1 = new File("temp.jpg");

			String url = product.getImageUrl();
			URL url1 = new URL(url);

			String filename = url.substring(url.lastIndexOf("/") + 1);
			try {
				BufferedImage img = ImageIO.read(url1);
				ImageIO.write(img, "jpg", file1);
			} catch (Exception e) {
				file1 = new File("no_image.jpg");
				System.out.println("Uploading no_image for " + product.getImageUrl());
			}
			client.putObject(awsConfig.getS3().getDefaultBucket(), filename, file1);
			product.setImageFileName(filename);
			product.setImageUrl(awsConfig.getS3().getEndPoint() + "/" + filename);
			rep.save(product);
			System.out.println(product.getId() + ":" + product.getImageUrl());
			writer.println(product.getId());
			writer.flush();

		}

		writer.close();
	}
}

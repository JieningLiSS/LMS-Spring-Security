package com.ss.orch.dao;

import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.stereotype.Component;

import com.ss.orch.entity.Book;
import com.ss.orch.entity.LibraryBranch;

@Component
public class BorrowerDao {
	
	@Autowired
	RestTemplate template;
	
	private static final String BASE = "http://BORROWER-SERVICE/lms/borrower/";

	public LibraryBranch[] getBranches() {
		
		String url = BASE + "branches";
		return template.getForObject(
				url, LibraryBranch[].class);
	}
	
	public LibraryBranch[] getBranches(int cardNo) {
		
		String url = BASE + "borrowers/{cardNo}/branches";
		
		return template.getForObject(
				url, LibraryBranch[].class, cardNo);
	}
	
	public Book[] getBooks(int branchId) {
		
		String url = BASE + "branches/{branchId}/books";
		
		return template.getForObject(
				url, Book[].class, branchId);
	}
	
	public Book[] getBooks(int cardNo, int branchId) {
		
		String url = BASE 
				+ "borrowers/{cardNo}/branches/{branchId}/books";
		

		
		return template.getForObject(
				url, Book[].class, cardNo, branchId);
	}
	
	public void insertLoan(int cardNo, int branchId, int bookId) {
		
		String url = BASE 
				+ "borrowers/{cardNo}/branches/{branchId}/books/{bookId}";
		
		template.postForObject(
				url, null, Void.class, cardNo, branchId, bookId);
	}
	
	public void deleteLoan(int cardNo, int branchId, int bookId) {
		
		String url = BASE 
				+ "borrowers/{cardNo}/branches/{branchId}/books/{bookId}";
		
		template.delete(
				url, cardNo, branchId, bookId);
	}
}

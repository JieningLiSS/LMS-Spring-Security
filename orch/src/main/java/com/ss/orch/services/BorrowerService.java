package com.ss.orch.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.ss.orch.dao.BorrowerDao;
import com.ss.orch.entity.*;

@Service
public class BorrowerService {

	@Autowired
	private BorrowerDao borrowerDao;
	
	
	public LibraryBranch[] getBranches() {

		return borrowerDao.getBranches();
	}
	
	public LibraryBranch[] getBranches(int cardNo) {
	
		return borrowerDao.getBranches(cardNo);
	}
	
	public Book[] getBooks(int branchId) {
		
		return borrowerDao.getBooks(branchId);
	}
	
	public Book[] getBooks(int cardNo, int branchId) {
		
		return borrowerDao.getBooks(cardNo, branchId);
	}
	
	public void insertLoan(int cardNo, int branchId, int bookId) {
		borrowerDao.insertLoan(cardNo, branchId, bookId);
	}
	
	public void deleteLoan(int cardNo, int branchId, int bookId) {
		borrowerDao.deleteLoan(cardNo, branchId, bookId);
	}
}

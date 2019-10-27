package com.ss.orch.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.ss.orch.entity.Author;
import com.ss.orch.entity.Book;
import com.ss.orch.entity.BookCopies;
import com.ss.orch.entity.BookLoans;
import com.ss.orch.entity.Borrower;
import com.ss.orch.entity.LibraryBranch;
import com.ss.orch.entity.Publisher;
import com.ss.orch.entity.ReadBookCopies;
import com.ss.orch.entity.ReadBookLoanData;

@RestController
@EnableEurekaClient
@RequestMapping("/lms*")
@Produces({"application/xml", "application/json"})
@Consumes({"application/xml", "application/json"})
public class OrchController
{
	
	@Autowired
	JdbcUserDetailsManager jdbcUserDetailsManager;
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	RestTemplate rt;
	private final String adminUri = "http://myELB-1700478388.us-east-2.elb.amazonaws.com/lms/admin";
	private final String libUri = "http://librarianLB-1418092176.us-east-2.elb.amazonaws.com/lms/librarian";
	
	
	
	@GetMapping("/health")
	public HttpStatus getHealth() {
		return HttpStatus.OK;
	}
	
	
	/************************Security*****************************/
	
	@GetMapping(path = "username/{username}", produces = {MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE})
	public ResponseEntity<UserDetails> readUserByName(@PathVariable String username)
	{
		// loadUser() never returns null, so 200 constantly
		UserDetails result = jdbcUserDetailsManager.loadUserByUsername(username);
		return new ResponseEntity<UserDetails>(result,HttpStatus.OK);
	}
	
	@PostMapping(path = "admin/username/{userName}/password/{password}")
	public ResponseEntity<UserDetails> createAdmin(@PathVariable("userName") String userName, @PathVariable("password") String password)
	{
		ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		UserDetails newUser = new User(
				userName,
				passwordEncoder.encode(password), 
				authorities);
		
		jdbcUserDetailsManager.createUser(newUser);
		return new ResponseEntity<UserDetails>(HttpStatus.NO_CONTENT);
	}
	
	@PostMapping(path = "librarian/username/{userName}/password/{password}")
	public ResponseEntity<UserDetails> createLibrarian(@PathVariable("userName") String userName, @PathVariable("password") String password)
	{
		ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority("ROLE_LIBRARIAN"));
		
		// Encode the password
		UserDetails newUser = new User(
				userName,
				passwordEncoder.encode(password), 
				authorities);
		
		jdbcUserDetailsManager.createUser(newUser);
		return new ResponseEntity<UserDetails>(HttpStatus.NO_CONTENT);
	}
	
	@PostMapping(path = "borrower/username/{userName}/password/{password}")
	public ResponseEntity<UserDetails> createBorrower(@PathVariable("userName") String userName, @PathVariable("password") String password)
	{
		ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority("ROLE_BORROWER"));
		UserDetails newUser = new User(
				userName,
				passwordEncoder.encode(password), 
				authorities);
		
		jdbcUserDetailsManager.createUser(newUser);
		return new ResponseEntity<UserDetails>(HttpStatus.NO_CONTENT);

	}
	
	
	


	/************************Admin*********************************/
	//createAuthor
	@RequestMapping(value = "/admin/authors", method = RequestMethod.POST)
	public ResponseEntity<Author> createAuthor(@RequestHeader("Accept") String accept,
			@RequestHeader("Content-Type") String contentType, @RequestBody Author author){
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Content-Type", contentType);
		headers.add("Accept", accept);
		HttpEntity<Author> request = new HttpEntity<Author>(author, headers);
		
	    try {
	    	return rt.exchange(adminUri + "/authors", HttpMethod.POST, request, Author.class);  
	        }catch(HttpStatusCodeException e) {
	            return new ResponseEntity<Author>(e.getStatusCode());
	        }		
	}
	
	//updateAuthor
	@RequestMapping(value = "/admin/authors/{authorId}", method = RequestMethod.PUT)
	public ResponseEntity<Author> updateAuthor(@RequestHeader("Accept") String accept,
			@RequestHeader("Content-Type") String contentType, @RequestBody Author author,@PathVariable int authorId){
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Content-Type", contentType);
		headers.add("Accept", accept);
		HttpEntity<Author> request = new HttpEntity<Author>(author, headers);
		
		  try {
				return rt.exchange(adminUri + "/authors/"+authorId, HttpMethod.PUT, request, Author.class);
		        }catch(HttpStatusCodeException e) {
		            return new ResponseEntity<Author>(e.getStatusCode());
		        }
	
	}
	
	//deleteAuthor
	@RequestMapping(value = "/admin/authors/{authorId}", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteAuthor(@PathVariable int authorId){
		RequestEntity<HttpStatus> request = new RequestEntity<>(HttpMethod.DELETE,URI.create(adminUri + "/authors/" + authorId));	
		  try {
 			return rt.exchange(request, HttpStatus.class);	
		        }catch(HttpStatusCodeException e) {
		            return new ResponseEntity<HttpStatus>(e.getStatusCode());
		        }		  
	}
	
	//getAllPublishers
	@RequestMapping(value = "/admin/publishers", method = RequestMethod.GET)
	public ResponseEntity<Publisher> getAllPublishers(@RequestHeader("Accept") String accept,
			@RequestHeader("Content-Type") String contentType, @RequestBody Publisher publisher){
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Content-Type", contentType);
		headers.add("Accept", accept);
		HttpEntity<Publisher> request = new HttpEntity<Publisher>(publisher, headers);
		
		  try {
			  return rt.exchange(adminUri + "/publishers", HttpMethod.GET, request, Publisher.class);
		        }catch(HttpStatusCodeException e) {
		            return new ResponseEntity<Publisher>(e.getStatusCode());
		        }
	}

	//createPublisher
	@RequestMapping(value = "/admin/publishers", method = RequestMethod.POST)
	public ResponseEntity<Publisher> createPublisher(@RequestHeader("Accept") String accept,
			@RequestHeader("Content-Type") String contentType, @RequestBody Publisher publisher){
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Content-Type", contentType);
		headers.add("Accept", accept);
		HttpEntity<Publisher> request = new HttpEntity<Publisher>(publisher, headers);
		
		  try {
			  return rt.exchange(adminUri + "/publishers", HttpMethod.POST, request, Publisher.class);
		        }catch(HttpStatusCodeException e) {
		            return new ResponseEntity<Publisher>(e.getStatusCode());
		        }
	}
	
	//updatePublisher
	@RequestMapping(value = "/admin/publishers/{publisherId}", method = RequestMethod.PUT)
	public ResponseEntity<Publisher> updatePublisher(@RequestHeader("Accept") String accept,
			@RequestHeader("Content-Type") String contentType, @RequestBody Publisher publisher,@PathVariable int publisherId){
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Content-Type", contentType);
		headers.add("Accept", accept);
		HttpEntity<Publisher> request = new HttpEntity<Publisher>(publisher, headers);
		
		  try {
				return rt.exchange(adminUri + "/publishers/"+publisherId, HttpMethod.PUT, request, Publisher.class);
		        }catch(HttpStatusCodeException e) {
		            return new ResponseEntity<Publisher>(e.getStatusCode());
		        }
	
	}
	
	//deletePublisher
		@RequestMapping(value = "/admin/publishers/{publisherId}", method = RequestMethod.DELETE)
		public ResponseEntity<HttpStatus> deletePublisher(@PathVariable int publisherId){
			RequestEntity<HttpStatus> request = new RequestEntity<>(HttpMethod.DELETE,URI.create(adminUri + "/publishers/" + publisherId));		
			  try {
				  return rt.exchange(request, HttpStatus.class);	
			        }catch(HttpStatusCodeException e) {
			            return new ResponseEntity<HttpStatus>(e.getStatusCode());
			        }
		}

	//createBook
	@RequestMapping(value = "/admin/books", method = RequestMethod.POST)
	public ResponseEntity<Book> createBook(@RequestHeader("Accept") String accept,
			@RequestHeader("Content-Type") String contentType, @RequestBody Book book){
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Content-Type", contentType);
		headers.add("Accept", accept);
		HttpEntity<Book> request = new HttpEntity<Book>(book, headers);
			
		  try {
			  return rt.exchange(adminUri + "/books", HttpMethod.POST, request, Book.class);
		        }catch(HttpStatusCodeException e) {
		            return new ResponseEntity<Book>(e.getStatusCode());
		        }
		
	}
	
	//updateBook
	@RequestMapping(value = "/admin/books/{bookId}", method = RequestMethod.PUT)
	public ResponseEntity<Book> updateBook(@RequestHeader("Accept") String accept,
			@RequestHeader("Content-Type") String contentType, @RequestBody Book book,@PathVariable int bookId){
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Content-Type", contentType);
		headers.add("Accept", accept);
		HttpEntity<Book> request = new HttpEntity<Book>(book, headers);
	
		  try {
				return rt.exchange(adminUri + "/books/"+bookId, HttpMethod.PUT, request, Book.class);
		        }catch(HttpStatusCodeException e) {
		            return new ResponseEntity<Book>(e.getStatusCode());
		        }
	}
	
	//deleteBook
	@RequestMapping(value = "/admin/books/{bookId}", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteBook(@PathVariable int bookId){
		RequestEntity<HttpStatus> request = new RequestEntity<>(HttpMethod.DELETE,URI.create(adminUri + "/books/" + bookId));	
		 try {
				return rt.exchange(request, HttpStatus.class);	
		        }catch(HttpStatusCodeException e) {
		            return new ResponseEntity<HttpStatus>(e.getStatusCode());
		        }
			}
	
	//createBorrower
	@RequestMapping(value = "/admin/borrowers", method = RequestMethod.POST)
	public ResponseEntity<Borrower> createBorrower(@RequestHeader("Accept") String accept,
			@RequestHeader("Content-Type") String contentType, @RequestBody Borrower borrower){
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Content-Type", contentType);
		headers.add("Accept", accept);
		HttpEntity<Borrower> request = new HttpEntity<Borrower>(borrower, headers);
		
		 try {
			 return rt.exchange(adminUri + "/borrowers", HttpMethod.POST, request, Borrower.class);
		        }catch(HttpStatusCodeException e) {
		            return new ResponseEntity<Borrower>(e.getStatusCode());
		        }
			
	}
	
	//updateBorrower
	@RequestMapping(value = "/admin/borrowers/{cardNo}", method = RequestMethod.PUT)
	public ResponseEntity<Borrower> updateBorrower(@RequestHeader("Accept") String accept,
			@RequestHeader("Content-Type") String contentType, @RequestBody Borrower borrower,@PathVariable int cardNo){
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Content-Type", contentType);
		headers.add("Accept", accept);
		HttpEntity<Borrower> request = new HttpEntity<Borrower>(borrower, headers);
	
		 try {
				return rt.exchange(adminUri + "/borrowers/"+cardNo, HttpMethod.PUT, request, Borrower.class);
		        }catch(HttpStatusCodeException e) {
		            return new ResponseEntity<Borrower>(e.getStatusCode());
		        }
			
	}

	//deleteBorrower
	@RequestMapping(value = "/admin/borrowers/{cardNo}", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteBorrower(@PathVariable int cardNo){
		RequestEntity<HttpStatus> request = new RequestEntity<>(HttpMethod.DELETE,URI.create(adminUri + "/borrowers/" + cardNo));

		 try {
				return rt.exchange(request, HttpStatus.class);	
		        }catch(HttpStatusCodeException e) {
		            return new ResponseEntity<HttpStatus>(e.getStatusCode());
		        }
	}
		
	//createLibraryBranch
	@RequestMapping(value = "/admin/libraryBranches", method = RequestMethod.POST)
	public ResponseEntity<LibraryBranch> createLibraryBranch(@RequestHeader("Accept") String accept,
			@RequestHeader("Content-Type") String contentType, @RequestBody LibraryBranch libraryBranch){
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Content-Type", contentType);
		headers.add("Accept", accept);
		HttpEntity<LibraryBranch> request = new HttpEntity<LibraryBranch>(libraryBranch, headers);
		
		 try {
			 return rt.exchange(adminUri + "/libraryBranches", HttpMethod.POST, request, LibraryBranch.class);
		        }catch(HttpStatusCodeException e) {
		            return new ResponseEntity<LibraryBranch>(e.getStatusCode());
		        }
	}
	
	//updateLibraryBranch
	@RequestMapping(value = "/admin/libraryBranches/{branchId}", method = RequestMethod.PUT)
	public ResponseEntity<LibraryBranch> updateLibraryBranch(@RequestHeader("Accept") String accept,
			@RequestHeader("Content-Type") String contentType, @RequestBody LibraryBranch libraryBranch,@PathVariable int branchId){
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Content-Type", contentType);
		headers.add("Accept", accept);
		HttpEntity<LibraryBranch> request = new HttpEntity<LibraryBranch>(libraryBranch, headers);
	
		 try {
				return rt.exchange(adminUri + "/libraryBranches/"+branchId, HttpMethod.PUT, request, LibraryBranch.class);
		        }catch(HttpStatusCodeException e) {
		            return new ResponseEntity<LibraryBranch>(e.getStatusCode());
		        }
	}
	
	//deleteLibraryBranch
	@RequestMapping(value = "/admin/libraryBranches/{branchId}", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteLibraryBranch(@PathVariable int branchId){
		RequestEntity<HttpStatus> request = new RequestEntity<>(HttpMethod.DELETE,URI.create(adminUri + "/libraryBranches/" + branchId));

		 try {
				return rt.exchange(request, HttpStatus.class);	
		        }catch(HttpStatusCodeException e) {
		            return new ResponseEntity<HttpStatus>(e.getStatusCode());
		        }
	}
		
	//overrideDuedate
	@RequestMapping(value = "/admin/duedates", method = RequestMethod.PUT)
	public ResponseEntity<BookLoans> overrideDuedate(@RequestHeader("Accept") String accept,
				@RequestHeader("Content-Type") String contentType, @RequestBody BookLoans bookLoans){
			MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
			headers.add("Content-Type", contentType);
			headers.add("Accept", accept);
			HttpEntity<BookLoans> request = new HttpEntity<BookLoans>(bookLoans, headers);
		
			 try {
					return rt.exchange(adminUri + "/duedates", HttpMethod.PUT, request, BookLoans.class);
			        }catch(HttpStatusCodeException e) {
			            return new ResponseEntity<BookLoans>(e.getStatusCode());
			        }
		}
	
	
	
	/******************************Librarian*********************************/
	//getAllLibraryBranch
	@GetMapping(path="/librarian/list",produces = {"application/json","application/xml"})
    public ResponseEntity<?>  getAllLibraryBranch(){
        ResponseEntity<List<LibraryBranch>> libraryBranchList =  rt.exchange(libUri+"/list",
                HttpMethod.GET,null, new ParameterizedTypeReference<List<LibraryBranch>>() {
                      
        } );
        return new ResponseEntity<List<LibraryBranch>>(libraryBranchList.getBody(),HttpStatus.OK);
    }
    
	//getLibraryBranchById
    @GetMapping(path="/librarian/id/{libraryBranchId}",produces = {"application/json","application/xml"})
    public ResponseEntity<?> getLibraryBranchById(@RequestHeader("Content-Type") String contentType, @PathVariable int libraryBranchId){
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.add("Content-Type", contentType);
        try {
            return rt.exchange(libUri+"/id/"+libraryBranchId, 
                    HttpMethod.GET,new HttpEntity<Object>(headers), LibraryBranch.class );
            
        }catch(HttpStatusCodeException e) {
            return new ResponseEntity<LibraryBranch>(e.getStatusCode());
        }
     
    }
    
    @PutMapping(path="/librarian/branchChange")
    public ResponseEntity<?> updateLibraryBranch(@RequestHeader("Accept") String accept,@RequestHeader("Content-Type") String contentType, @RequestBody LibraryBranch libraryBranch) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.add("Accept", accept);
        headers.add("Content-Type", contentType);
       
        try{
            return rt.exchange(libUri+"/branchChange", 
                HttpMethod.PUT, new HttpEntity<Object>(libraryBranch,headers) , LibraryBranch.class );
        }catch(HttpStatusCodeException e) {
            return new ResponseEntity<LibraryBranch>(e.getStatusCode());
        }
        
    }
    
    @GetMapping(path="/librarian/id/{libraryBranchId}/book-list",produces = {"application/json","application/xml"})
    public ResponseEntity<?> getBookListByBranchId(@PathVariable int libraryBranchId){
        try{
            ResponseEntity<List<BookCopies>> bookCopiesList = rt.exchange(libUri+"/id/"+libraryBranchId+"/book-list",      
                HttpMethod.GET,null, new ParameterizedTypeReference<List<BookCopies>>() {
            
        } );
            return bookCopiesList;
        }catch(HttpStatusCodeException e) {
            return new ResponseEntity<List<BookCopies>>(e.getStatusCode());
        }
    
    }
    
    @PutMapping(path="/librarian/copyAddition",consumes={"application/json","application/xml"})
    public ResponseEntity<?> updateNoOfCopies(@RequestHeader("Accept") String accept,@RequestHeader("Content-Type") String contentType , @RequestBody ReadBookCopies readBookCopies){
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.add("Accept", accept);
        headers.add("Content-Type", contentType);
        try {
        return rt.exchange(libUri+"/copyAddition", 
                HttpMethod.PUT,new HttpEntity<Object>(readBookCopies,headers), String.class
            );   
        }catch(HttpStatusCodeException e) {
            return new ResponseEntity<String>(e.getStatusCode());
        }
    }
    

}

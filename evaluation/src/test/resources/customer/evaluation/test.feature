Feature: Customer evaluation
   Evaluate a customer's information to determine age and wealth

   Scenario: Evaluating an underage customer
      Given Some customer information
      When The customer is not of legal age
      Then Flag customer as underage
   
   Scenario: Evaluating a poor customer
      Given Some customer information
      When The customer has less than 1000 dollars
      Then Flag customer as poor
      
   Scenario: Evaluating a rich customer
      Given Some customer information
      When The customer has more than 999 dollars
      Then Flag customer as rich
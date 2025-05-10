#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <omp.h>
#include <cstdlib>
#include <papi.h>
#include <chrono>
using namespace std::chrono;
using namespace std;






#define SYSTEMTIME clock_t


void OnMult(int m_ar, int m_br, int m_cr) {
  
   SYSTEMTIME Time1, Time2;
  
   char st[100];
   double temp;
   int i, j, k;


   double *pha, *phb, *phc;
      
   pha = (double *)malloc((m_ar * m_br) * sizeof(double));
   phb = (double *)malloc((m_br * m_cr) * sizeof(double));
   phc = (double *)malloc((m_ar * m_cr) * sizeof(double));


   for(i=0; i<m_ar; i++)
       for(j=0; j<m_br; j++)
           pha[i*m_br + j] = (double)1.0;


   for(i=0; i<m_br; i++)
       for(j=0; j<m_cr; j++)
           phb[i*m_cr + j] = (double)(i+1);
      
   for(i=0; i<m_ar; i++)
       for(j=0; j<m_cr; j++)
           pha[i*m_cr + j] = (double)1.0;




   Time1 = clock();


   // # pragma omp parallel for private(i,j,temp) collapse(2)
   for(i=0; i<m_ar; i++) {
       for( j=0; j<m_cr; j++) {   
           temp = 0;
           // # pragma omp simd private(k) reduction(+:temp)
           for( k=0; k<m_br; k++) {   
               temp += pha[i*m_br+k] * phb[k*m_cr+j];
           }
           phc[i*m_cr+j]=temp;
       }
   }




   Time2 = clock();
   sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
   cout << st;


   // display 10 elements of the result matrix tto verify correctness
   cout << "Result matrix: " << endl;
   for(i=0; i<1; i++) {   
       for(j=0; j<min(10,m_br); j++)
       cout << phc[j] << " ";
   }
   cout << endl;


   free(pha);
   free(phb);
   free(phc);


}


// add code here for line x line matriz multiplication
void OnMultLine(int m_ar, int m_br, int m_cr) {


   SYSTEMTIME Time1, Time2;
  
   char st[100];
   double temp;
   int i, j, k;


   double *pha, *phb, *phc;
      
   pha = (double *)malloc((m_ar * m_br) * sizeof(double));
   phb = (double *)malloc((m_br * m_cr) * sizeof(double));
   phc = (double *)malloc((m_ar * m_cr) * sizeof(double));


   for(i=0; i<m_ar; i++)
       for(j=0; j<m_br; j++)
           pha[i*m_br + j] = (double)1.0;


   for(i=0; i<m_br; i++)
       for(j=0; j<m_cr; j++)
           phb[i*m_cr + j] = (double)(i+1);
      
   for(i=0; i<m_ar; i++)
       for(j=0; j<m_cr; j++)
           pha[i*m_cr + j] = (double)1.0;


  
   Time1 = clock();


   for(i=0; i<m_ar; i++){
       for(k=0; k<m_br; k++){
           for(j=0; j<m_cr; j++){     
               phc[i*m_cr+j] += pha[i*m_br+k] * phb[k*m_cr+j];
           }
       }
   }


   Time2 = clock();
   sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
   cout << st;


   // display 10 elements of the result matrix tto verify correctness
   cout << "Result matrix: " << endl;
   for(i=0; i<1; i++) {   
       for(j=0; j<min(10,m_cr); j++)
       cout << phc[j] << " ";
   }
   cout << endl;


   free(pha);
   free(phb);
   free(phc);
  
}


// add code here for block x block matriz multiplication
void OnMultBlock(int m_ar, int m_br, int m_cr, int bkSize) {


   SYSTEMTIME Time1, Time2;
  
   char st[100];
   double temp;
   int i, j, k, bi, bj, bk;


   double *pha, *phb, *phc;
      
   pha = (double *)malloc((m_ar * m_br) * sizeof(double));
   phb = (double *)malloc((m_br * m_cr) * sizeof(double));
   phc = (double *)malloc((m_ar * m_cr) * sizeof(double));


   for(i=0; i<m_ar; i++)
       for(j=0; j<m_br; j++)
           pha[i*m_br + j] = (double)1.0;


   for(i=0; i<m_br; i++)
       for(j=0; j<m_cr; j++)
           phb[i*m_cr + j] = (double)(i+1);
      
   for(i=0; i<m_ar; i++)
       for(j=0; j<m_cr; j++)
           pha[i*m_cr + j] = (double)1.0;




   Time1 = clock();


   for(bi=0; bi<m_ar; bi+=bkSize){
       for(bj=0; bj<m_cr; bj+=bkSize){
           for(bk=0; bk<m_br; bk+=bkSize){
               for(i=bi; i<min(bkSize+bi, m_ar); i++){
                   for(k=bk; k<min(bkSize+bk, m_br); k++){
                       for(j=bj; j<min(bkSize+bj, m_cr); j++){
                           phc[i*m_cr+j] += pha[i*m_br+k] * phb[k*m_cr+j];
                       }
                   }
               }  
           }
       }
   }


   Time2 = clock();
   sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
   cout << st;


   // display 10 elements of the result matrix tto verify correctness
   cout << "Result matrix: " << endl;
   for(i=0; i<1; i++) {   
       for(j=0; j<min(10,m_cr); j++)
       cout << phc[j] << " ";
   }
   cout << endl;


   free(pha);
   free(phb);
   free(phc);   
}






// add code here for line x line matriz multiplication
void P1OnMultLine(int m_ar, int m_br, int m_cr) {
  
   char st[100];
   double temp;
   int i, j, k;


   double *pha, *phb, *phc;
      
   pha = (double *)malloc((m_ar * m_br) * sizeof(double));
   phb = (double *)malloc((m_br * m_cr) * sizeof(double));
   phc = (double *)malloc((m_ar * m_cr) * sizeof(double));


   for(i=0; i<m_ar; i++)
       for(j=0; j<m_br; j++)
           pha[i*m_br + j] = (double)1.0;


   for(i=0; i<m_br; i++)
       for(j=0; j<m_cr; j++)
           phb[i*m_cr + j] = (double)(i+1);
      
   for(i=0; i<m_ar; i++)
       for(j=0; j<m_cr; j++)
           pha[i*m_cr + j] = (double)1.0;


   int num_threads=1;
   auto Time1 = high_resolution_clock::now();


   # pragma omp parallel
   {
       num_threads = omp_get_num_threads();
       # pragma omp for
       for (int i = 0; i < m_ar ; i ++) {
           for (int k = 0; k < m_br ; k ++) {
               for (int j = 0; j < m_cr ; j ++) { 
                   phc[i*m_cr+j] += pha[i*m_br+k] * phb[k*m_cr+j];
               }
           }
       }
   }
   auto Time2 = high_resolution_clock::now();
   cout << "Time: " << duration<double>(Time2 - Time1).count() << " seconds" << endl;
   cout << "Number of Threads Used: " << num_threads << endl;


   // display 10 elements of the result matrix tto verify correctness
   cout << "Result matrix: " << endl;
   for(i=0; i<1; i++) {   
       for(j=0; j<min(10,m_cr); j++)
       cout << phc[j] << " ";
   }
   cout << endl;


   free(pha);
   free(phb);
   free(phc);
  
}




// add code here for line x line matriz multiplication
void P2OnMultLine(int m_ar, int m_br, int m_cr) {


  
   char st[100];
   double temp;
   int i, j, k;


   double *pha, *phb, *phc;
      
   pha = (double *)malloc((m_ar * m_br) * sizeof(double));
   phb = (double *)malloc((m_br * m_cr) * sizeof(double));
   phc = (double *)malloc((m_ar * m_cr) * sizeof(double));


   for(i=0; i<m_ar; i++)
       for(j=0; j<m_br; j++)
           pha[i*m_br + j] = (double)1.0;


   for(i=0; i<m_br; i++)
       for(j=0; j<m_cr; j++)
           phb[i*m_cr + j] = (double)(i+1);
      
   for(i=0; i<m_ar; i++)
       for(j=0; j<m_cr; j++)
           pha[i*m_cr + j] = (double)1.0;


   int num_threads=1;
   auto Time1 = high_resolution_clock::now();


   # pragma omp parallel
   {
       num_threads = omp_get_num_threads();
       for (int i = 0; i < m_ar ; i ++) {
           for (int k = 0; k < m_br ; k ++) {
               # pragma omp for
               for (int j = 0; j < m_cr ; j ++) { 
                   phc[i*m_cr+j] += pha[i*m_br+k] * phb[k*m_cr+j];
               }
           }
       }
   }


   auto Time2 = high_resolution_clock::now();
   cout << "Time: " << duration<double>(Time2 - Time1).count() << " seconds" << endl;
   cout << "Number of Threads Used: " << num_threads << endl;




   // display 10 elements of the result matrix tto verify correctness
   cout << "Result matrix: " << endl;
   for(i=0; i<1; i++) {   
       for(j=0; j<min(10,m_cr); j++)
       cout << phc[j] << " ";


   }
   cout << endl;


   free(pha);
   free(phb);
   free(phc);
  
}




void handle_error (int retval)
{
 printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
 exit(1);
}


void init_papi() {
 int retval = PAPI_library_init(PAPI_VER_CURRENT);
 if (retval != PAPI_VER_CURRENT && retval < 0) {
   printf("PAPI library version mismatch!\n");
   exit(1);
 }
 if (retval < 0) handle_error(retval);


 std::cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
           << " MINOR: " << PAPI_VERSION_MINOR(retval)
           << " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";
}




int main (int argc, char *argv[])
{


   char c;
   int lin, col, colC, blockSize;
   int op, sq;
  
   int EventSet = PAPI_NULL;
   long long values[2];
   int ret;
  


   ret = PAPI_library_init( PAPI_VER_CURRENT );
   if ( ret != PAPI_VER_CURRENT )
       std::cout << "FAIL" << endl;




   ret = PAPI_create_eventset(&EventSet);
       if (ret != PAPI_OK) cout << "ERROR: create eventset" << endl;




   ret = PAPI_add_event(EventSet,PAPI_L1_DCM );
   if (ret != PAPI_OK) cout << "ERROR: PAPI_L1_DCM" << endl;




   ret = PAPI_add_event(EventSet,PAPI_L2_DCM);
   if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCM" << endl;




   op=1;
   do {
       cout << endl << "1. Multiplication" << endl;
       cout << "2. Line Multiplication" << endl;
       cout << "3. Block Multiplication" << endl;
       cout << "4. Line Multiplication P1" << endl;
       cout << "5. Line Multiplication P2" << endl;
       cout << "Selection?: ";
       cin >> op;
       if (op == 0)
           break;
       cout << "Non-square matrices? (0 - No, 1 Yes): ";
       cin >> sq;
       if (sq == 0) {
           printf("Dimensions: lins=cols ? ");
           cin >> lin;
           col = lin;
           colC = lin;
       } else {
           printf("Dimensions: lins A? ");
           cin >> lin;
           printf("Dimensions: cols A = lins B? ");
           cin >> col;
           printf("Dimensions: cols B? ");
           cin >> colC;
       }




       // Start counting
       ret = PAPI_start(EventSet);
       if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;


       switch (op){
           case 1:
               OnMult(lin, col, colC);
               break;
           case 2:
               OnMultLine(lin, col, colC); 
               break;
           case 3:
               cout << "Block Size? ";
               cin >> blockSize;
               OnMultBlock(lin, col, colC, blockSize); 
               break;
           case 4:
               P1OnMultLine(lin, col, colC);
               break;
           case 5:
               P2OnMultLine(lin, col, colC);
               break;
       }


       ret = PAPI_stop(EventSet, values);
       if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
       printf("L1 DCM: %lld \n",values[0]);
       printf("L2 DCM: %lld \n",values[1]);


       ret = PAPI_reset( EventSet );
       if ( ret != PAPI_OK )
           std::cout << "FAIL reset" << endl;






   } while (op != 0);


   ret = PAPI_remove_event( EventSet, PAPI_L1_DCM );
   if ( ret != PAPI_OK )
       std::cout << "FAIL remove event" << endl;


   ret = PAPI_remove_event( EventSet, PAPI_L2_DCM );
   if ( ret != PAPI_OK )
       std::cout << "FAIL remove event" << endl;


   ret = PAPI_destroy_eventset( &EventSet );
   if ( ret != PAPI_OK )
       std::cout << "FAIL destroy" << endl;


}

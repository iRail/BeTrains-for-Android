package tof.cv.bo;

public class Message{

		private String auteur;
		private String body;
		private String time;
		private String train_id;

		public Message(String pauteur, String pbody, String ptime, String ptrain_id){
			this.auteur=pauteur;
			this.body=pbody;
			this.time=ptime;
			this.train_id=ptrain_id;
		}

		public String getauteur() {
			return this.auteur;
		}

		public String getbody() {
			return this.body;
		}

		public String gettime() {
			return this.time;
		}

		public String gettrain_id() {
			return this.train_id;
		}



		


}



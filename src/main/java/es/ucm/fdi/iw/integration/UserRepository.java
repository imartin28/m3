package es.ucm.fdi.iw.integration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.ucm.fdi.iw.model.User;

@Repository("userRepository")
public interface UserRepository extends JpaRepository<User, Long> {
	 User findByEmail(String email); //Sólo con hacer un findBy'nombreDelAtributo'(Tipo nombre); te busca los usuarios con ese campo
	 User findByNickname(String nickname);
	 User findById(long id);
	 
	 /*
	 List<User> findByEmailOrNicknameOrNameOrLastNameOrEmailLikeOrNicknameLikeOrNameLikeOrLastNameLike(
		 String searchText, String searchText2, String searchText3, String searchText4, 
		 String searchText5, String searchText6, String searchText7, String searchText8
	);
	*/
}

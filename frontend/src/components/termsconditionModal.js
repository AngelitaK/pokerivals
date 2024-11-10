import { Modal, ModalOverlay, ModalContent, ModalHeader, ModalCloseButton, ModalBody, ModalFooter, Text, Button } from "@chakra-ui/react";

const TermsAndConditionsModal = ({ isOpen, onClose }) => {
  return (
    <Modal isOpen={isOpen} onClose={onClose} size="lg">
      <ModalOverlay />
      <ModalContent>
        <ModalHeader>Terms & Conditions</ModalHeader>
        <ModalCloseButton />
        <ModalBody>
          <Text fontWeight="bold">1. Acceptance of Terms</Text>
          <Text>
            By using the top-up service to purchase PokeCredits, you acknowledge and agree to these terms and conditions. Please read them carefully.
          </Text>

          <Text fontWeight="bold" mt={4}>2. No Refunds</Text>
          <Text>
            All purchases of PokeCredits are final and non-refundable. Once you have confirmed your transaction and PokeCredits have been credited to your account, you will not be able to reverse or refund the transaction.
          </Text>

          <Text fontWeight="bold" mt={4}>3. User Responsibility</Text>
          <Text>
            - You are responsible for ensuring that any funds used for purchasing PokeCredits are legally obtained and owned by you. <br />
            - Only individuals aged 18 or older (or the age of legal consent in your jurisdiction) are permitted to participate in purchasing PokeCredits. <br />
          </Text>

          <Text fontWeight="bold" mt={4}>4. Betting Risks</Text>
          <Text>
            All bets placed using PokeCredits are at your own risk. PokeCredits used for betting are not refundable, even if you lose the bet. The platform is not responsible for any losses incurred while using PokeCredits for betting or other activities on the platform.
          </Text>
        </ModalBody>
        <ModalFooter>
          <Button colorScheme="blue" mr={3} onClick={onClose}>
            Close
          </Button>
        </ModalFooter>
      </ModalContent>
    </Modal>
  );
};

export default TermsAndConditionsModal;
